package com.aiplatform.backend.service;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 构造函数，注入所需的数据访问层依赖。
     *
     * @param knowledgeBaseMapper           知识库 Mapper
     * @param kbDocumentMapper              知识库文档 Mapper
     * @param projectKnowledgeConfigMapper  项目知识库配置 Mapper
     */
    public KnowledgeBaseService(KnowledgeBaseMapper knowledgeBaseMapper,
                                KbDocumentMapper kbDocumentMapper,
                                ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.kbDocumentMapper = kbDocumentMapper;
        this.projectKnowledgeConfigMapper = projectKnowledgeConfigMapper;
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
     * 查询指定知识库下的文档列表。
     *
     * @param kbId 知识库ID
     * @return 按ID升序排列的文档列表
     */
    public List<KbDocument> listDocuments(Long kbId) {
        return kbDocumentMapper.selectList(Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getKbId, kbId).orderByAsc(KbDocument::getId));
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
     *
     * @param projectId 项目ID
     * @return 该项目关联的知识库配置列表
     */
    public List<ProjectKnowledgeConfig> listProjectConfigs(Long projectId) {
        return projectKnowledgeConfigMapper.selectList(Wrappers.<ProjectKnowledgeConfig>lambdaQuery()
                .eq(ProjectKnowledgeConfig::getProjectId, projectId).orderByAsc(ProjectKnowledgeConfig::getId));
    }
}
