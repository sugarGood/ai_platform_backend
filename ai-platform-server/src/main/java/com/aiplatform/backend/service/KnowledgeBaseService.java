package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.common.exception.KnowledgeBaseNotFoundException;
import com.aiplatform.backend.dto.CreateKbDocumentRequest;
import com.aiplatform.backend.dto.CreateKnowledgeBaseRequest;
import com.aiplatform.backend.entity.KbDocument;
import com.aiplatform.backend.entity.KnowledgeBase;
import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.aiplatform.backend.mapper.KbDocumentMapper;
import com.aiplatform.backend.mapper.KnowledgeBaseMapper;
import com.aiplatform.backend.mapper.ProjectKnowledgeConfigMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库业务服务。
 *
 * <p>提供知识库管理、文档管理和项目知识库配置管理的核心业务逻辑。</p>
 */
@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KbDocumentMapper kbDocumentMapper;
    private final ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper;
    private final KnowledgeIngestAsyncService knowledgeIngestAsyncService;
    private final MinioKnowledgeStorageService minioKnowledgeStorageService;
    private final WebClient agentWebClient;
    private final boolean knowledgeIngestEnabled;

    /**
     * 构造函数，注入所需的数据访问层依赖。
     *
     * @param knowledgeBaseMapper           知识库 Mapper
     * @param kbDocumentMapper              知识库文档 Mapper
     * @param projectKnowledgeConfigMapper  项目知识库配置 Mapper
     * @param knowledgeIngestAsyncService   向量化异步触发
     * @param minioKnowledgeStorageService  MinIO 知识库文件存储
     * @param agentWebClient                内网 Agent，用于检索测试（向量检索）
     * @param knowledgeIngestEnabled        是否在保存后立即触发向量化
     */
    public KnowledgeBaseService(KnowledgeBaseMapper knowledgeBaseMapper,
                                KbDocumentMapper kbDocumentMapper,
                                ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper,
                                KnowledgeIngestAsyncService knowledgeIngestAsyncService,
                                MinioKnowledgeStorageService minioKnowledgeStorageService,
                                @Qualifier("agentWebClient") WebClient agentWebClient,
                                @Value("${knowledge.ingest.enabled:true}") boolean knowledgeIngestEnabled) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.kbDocumentMapper = kbDocumentMapper;
        this.projectKnowledgeConfigMapper = projectKnowledgeConfigMapper;
        this.knowledgeIngestAsyncService = knowledgeIngestAsyncService;
        this.minioKnowledgeStorageService = minioKnowledgeStorageService;
        this.agentWebClient = agentWebClient;
        this.knowledgeIngestEnabled = knowledgeIngestEnabled;
    }

    /**
     * 创建知识库。
     *
     * <p>根据请求参数创建新的知识库，默认向量化模型为 bge-m3，注入模式为 ON_DEMAND。</p>
     *
     * @param request 创建知识库请求
     * @return 新创建的知识库实体
     */
    public KnowledgeBase create(CreateKnowledgeBaseRequest request) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(request.name());
        kb.setDescription(request.description());
        kb.setScope(request.scope());
        kb.setProjectId(request.projectId());
        kb.setCategory(request.category());
        kb.setEmbeddingModel(request.embeddingModel() != null ? request.embeddingModel() : "bge-m3");
        kb.setDocCount(0);
        kb.setTotalChunks(0);
        kb.setInjectMode(request.injectMode() != null ? request.injectMode() : "ON_DEMAND");
        kb.setStatus("ACTIVE");
        knowledgeBaseMapper.insert(kb);
        return kb;
    }

    /**
     * 查询所有知识库列表。
     *
     * @return 按ID升序排列的知识库列表
     */
    public List<KnowledgeBase> list() {
        return knowledgeBaseMapper.selectList(Wrappers.<KnowledgeBase>lambdaQuery().orderByAsc(KnowledgeBase::getId));
    }

    /**
     * 按作用域查询知识库列表。
     *
     * @param scope 作用域：GLOBAL / PROJECT
     * @return 符合条件的知识库列表
     */
    public List<KnowledgeBase> listByScope(String scope) {
        return knowledgeBaseMapper.selectList(Wrappers.<KnowledgeBase>lambdaQuery()
                .eq(KnowledgeBase::getScope, scope).orderByAsc(KnowledgeBase::getId));
    }

    /**
     * 根据ID查询知识库，不存在则抛出异常。
     *
     * @param id 知识库ID
     * @return 知识库实体
     * @throws KnowledgeBaseNotFoundException 当知识库不存在时抛出
     */
    public KnowledgeBase getByIdOrThrow(Long id) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null) throw new KnowledgeBaseNotFoundException(id);
        return kb;
    }

    // ==================== 文档管理 ====================

    /**
     * 创建知识库文档。
     *
     * <p>在指定知识库下新增文档，初始状态为 PENDING，等待后续向量化处理。</p>
     *
     * @param request 创建文档请求
     * @return 新创建的文档实体
     * @throws KnowledgeBaseNotFoundException 当指定知识库不存在时抛出
     */
    public KbDocument createDocument(CreateKbDocumentRequest request) {
        getByIdOrThrow(request.kbId());
        KbDocument doc = new KbDocument();
        doc.setKbId(request.kbId());
        doc.setTitle(request.title());
        doc.setFileType(request.fileType());
        doc.setFilePath(request.filePath());
        doc.setFileSize(request.fileSize());
        doc.setChunkCount(0);
        doc.setHitCount(0);
        doc.setInjectMode(request.injectMode() != null ? request.injectMode() : "ON_DEMAND");
        doc.setRefProjects(0);
        doc.setStatus("PENDING");
        kbDocumentMapper.insert(doc);
        return doc;
    }

    /**
     * 上传文档到 MinIO（路径 {@code yyyy/MM/dd/文件名_时间戳.ext}）并落库，保存成功后异步触发 Agent 向量化入库。
     *
     * <p>{@code kb_documents.file_path} 存 MinIO 对象键；Agent 使用相同桶配置按该键拉取。</p>
     */
    public KbDocument uploadDocument(Long kbId, MultipartFile file, String title, String injectMode) {
        getByIdOrThrow(kbId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "INVALID_FILE", "文件不能为空");
        }

        String original = file.getOriginalFilename();
        String safeName = sanitizeFileName(original);
        String ext = fileExtension(safeName);
        String fileType = ext.isEmpty() ? "bin" : ext;

        String objectKey = minioKnowledgeStorageService.uploadKnowledgeObject(file, safeName);
        String docTitle = (title != null && !title.isBlank()) ? title.trim() : fileTitleFromName(safeName);

        KbDocument doc = new KbDocument();
        doc.setKbId(kbId);
        doc.setTitle(docTitle);
        doc.setFileType(fileType);
        doc.setFilePath(objectKey);
        doc.setFileSize(file.getSize());
        doc.setChunkCount(0);
        doc.setHitCount(0);
        doc.setInjectMode(injectMode != null ? injectMode : "ON_DEMAND");
        doc.setRefProjects(0);
        doc.setStatus("PENDING");
        kbDocumentMapper.insert(doc);

        if (knowledgeIngestEnabled) {
            knowledgeIngestAsyncService.runIngest(kbId, doc.getId(), docTitle, objectKey);
        }
        return doc;
    }

    /**
     * 向量解析失败或需重算时，按已有存储对象键（MinIO objectKey）重新异步向量化。
     *
     * <p>处理中（{@code PROCESSING}）的文档会拒绝重复提交，避免并发任务冲突。</p>
     */
    public KbDocument reingestDocument(Long kbId, Long docId) {
        getByIdOrThrow(kbId);
        KbDocument doc = kbDocumentMapper.selectOne(Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getKbId, kbId)
                .eq(KbDocument::getId, docId));
        if (doc == null) {
            throw new BusinessException(404, BizErrorCode.KB_DOCUMENT_NOT_FOUND, "文档不存在或不属于该知识库");
        }
        if ("PROCESSING".equals(doc.getStatus())) {
            throw new BusinessException(409, BizErrorCode.INGEST_IN_PROGRESS, "文档正在向量化处理中，请稍后再试");
        }
        if (doc.getFilePath() == null || doc.getFilePath().isBlank()) {
            throw new BusinessException(400, BizErrorCode.NO_OBJECT_KEY, "文档未关联存储对象，无法重新解析");
        }
        if (!knowledgeIngestEnabled) {
            throw new BusinessException(400, BizErrorCode.INGEST_DISABLED, "向量化任务未启用（knowledge.ingest.enabled=false）");
        }

        kbDocumentMapper.update(null, Wrappers.<KbDocument>lambdaUpdate()
                .set(KbDocument::getStatus, "PENDING")
                .set(KbDocument::getErrorMessage, null)
                .set(KbDocument::getChunkCount, 0)
                .eq(KbDocument::getId, docId)
                .eq(KbDocument::getKbId, kbId));

        doc.setStatus("PENDING");
        doc.setErrorMessage(null);
        doc.setChunkCount(0);

        knowledgeIngestAsyncService.runIngest(kbId, docId, doc.getTitle(), doc.getFilePath());
        return doc;
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "upload.bin";
        }
        String base = Paths.get(name).getFileName().toString();
        return base.replaceAll("[\\\\/:*?\"<>|\\x00-\\x1f]", "_");
    }

    private static String fileExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return "";
        }
        return filename.substring(i + 1).toLowerCase();
    }

    private static String fileTitleFromName(String filename) {
        int i = filename.lastIndexOf('.');
        if (i <= 0) {
            return filename;
        }
        return filename.substring(0, i);
    }

    /**
     * 查询指定知识库下的文档列表。
     *
     * @param kbId 知识库ID
     * @return 按ID升序排列的文档列表
     */
    public List<KbDocument> listDocuments(Long kbId) {
        return kbDocumentMapper.selectList(Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getKbId, kbId).orderByAsc(KbDocument::getId));
    }

    /**
     * 检索测试：转发至 Agent，使用与对话 RAG 相同的 Embedding + Qdrant 链路。
     */
    public Map<String, Object> searchKnowledgeBase(Long kbId, String query, int resultCount, Double scoreThreshold) {
        getByIdOrThrow(kbId);
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("kbId", kbId);
        req.put("query", query);
        req.put("resultCount", resultCount);
        if (scoreThreshold != null) {
            req.put("scoreThreshold", scoreThreshold);
        }
        try {
            Map<String, Object> resp = agentWebClient.post()
                    .uri("/api/knowledge/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            if (resp == null) {
                throw new BusinessException(502, "KB_SEARCH_FAILED", "知识库检索返回为空");
            }
            return resp;
        } catch (WebClientResponseException e) {
            String detail = e.getResponseBodyAsString();
            if (detail != null && detail.length() > 500) {
                detail = detail.substring(0, 500);
            }
            String msg = "知识库检索失败: " + (detail != null && !detail.isBlank() ? detail : e.getMessage());
            throw new BusinessException(e.getStatusCode().value(), "KB_SEARCH_FAILED", msg, e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(502, "KB_SEARCH_FAILED", "无法连接向量检索服务: " + e.getMessage(), e);
        }
    }

    // ==================== 项目知识库配置 ====================

    /**
     * 为项目启用全局知识库。
     *
     * <p>创建项目与全局知识库的关联配置，可指定检索权重。</p>
     *
     * @param projectId    项目ID
     * @param kbId         全局知识库ID
     * @param searchWeight 检索权重（0~1），默认为 1
     * @return 新创建的项目知识库配置
     */
    public ProjectKnowledgeConfig enableForProject(Long projectId, Long kbId, BigDecimal searchWeight) {
        ProjectKnowledgeConfig config = new ProjectKnowledgeConfig();
        config.setProjectId(projectId);
        config.setKbId(kbId);
        config.setSearchWeight(searchWeight != null ? searchWeight : BigDecimal.ONE);
        config.setStatus("ACTIVE");
        projectKnowledgeConfigMapper.insert(config);
        return config;
    }

    /**
     * 查询项目的知识库配置列表。
     */
    public List<ProjectKnowledgeConfig> listProjectConfigs(Long projectId) {
        return projectKnowledgeConfigMapper.selectList(Wrappers.<ProjectKnowledgeConfig>lambdaQuery()
                .eq(ProjectKnowledgeConfig::getProjectId, projectId).orderByAsc(ProjectKnowledgeConfig::getId));
    }

    /** 编辑知识库（仅更新非null字段）。 */
    public KnowledgeBase update(Long id, CreateKnowledgeBaseRequest request) {
        KnowledgeBase kb = getByIdOrThrow(id);
        if (request.name() != null) kb.setName(request.name());
        if (request.description() != null) kb.setDescription(request.description());
        if (request.category() != null) kb.setCategory(request.category());
        if (request.embeddingModel() != null) kb.setEmbeddingModel(request.embeddingModel());
        if (request.injectMode() != null) kb.setInjectMode(request.injectMode());
        knowledgeBaseMapper.updateById(kb);
        return kb;
    }

    /** 归档知识库（status → INACTIVE）。 */
    public KnowledgeBase archive(Long id) {
        KnowledgeBase kb = getByIdOrThrow(id);
        kb.setStatus("INACTIVE");
        knowledgeBaseMapper.updateById(kb);
        return kb;
    }

    /** 根据ID查询文档，不存在则抛出异常。 */
    public KbDocument getDocumentByIdOrThrow(Long id) {
        KbDocument doc = kbDocumentMapper.selectById(id);
        if (doc == null) throw new KnowledgeBaseNotFoundException(id);
        return doc;
    }

    /** 删除文档。 */
    public void deleteDocument(Long kbId, Long docId) {
        kbDocumentMapper.delete(Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getKbId, kbId)
                .eq(KbDocument::getId, docId));
    }

    /** 解绑项目知识库配置。 */
    public void disableProjectConfig(Long projectId, Long configId) {
        projectKnowledgeConfigMapper.delete(Wrappers.<ProjectKnowledgeConfig>lambdaQuery()
                .eq(ProjectKnowledgeConfig::getProjectId, projectId)
                .eq(ProjectKnowledgeConfig::getId, configId));
    }

    /** 更新RAG配置（embeddingModel、injectMode等）。 */
    public KnowledgeBase updateRagConfig(Long id, java.util.Map<String, String> config) {
        KnowledgeBase kb = getByIdOrThrow(id);
        if (config.containsKey("embeddingModel")) kb.setEmbeddingModel(config.get("embeddingModel"));
        if (config.containsKey("injectMode")) kb.setInjectMode(config.get("injectMode"));
        knowledgeBaseMapper.updateById(kb);
        return kb;
    }
}
