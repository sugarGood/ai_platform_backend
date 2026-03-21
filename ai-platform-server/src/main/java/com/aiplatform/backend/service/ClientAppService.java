package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.ClientAppNotFoundException;
import com.aiplatform.backend.dto.CreateClientAppRequest;
import com.aiplatform.backend.entity.ClientApp;
import com.aiplatform.backend.mapper.ClientAppMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 研发客户端业务服务。
 */
@Service
public class ClientAppService {

    private final ClientAppMapper clientAppMapper;

    /** 构造函数。 */
    public ClientAppService(ClientAppMapper clientAppMapper) {
        this.clientAppMapper = clientAppMapper;
    }

    /** 创建研发客户端。 */
    public ClientApp create(CreateClientAppRequest request) {
        ClientApp app = new ClientApp();
        app.setCode(request.code());
        app.setName(request.name());
        app.setIcon(request.icon());
        app.setSupportsMcp(request.supportsMcp() != null ? request.supportsMcp() : false);
        app.setSupportsCustomGateway(request.supportsCustomGateway() != null ? request.supportsCustomGateway() : false);
        app.setSetupInstruction(request.setupInstruction());
        app.setStatus("ACTIVE");
        clientAppMapper.insert(app);
        return app;
    }

    /** 查询所有客户端列表。 */
    public List<ClientApp> list() {
        return clientAppMapper.selectList(Wrappers.<ClientApp>lambdaQuery().orderByAsc(ClientApp::getId));
    }

    /** 根据 ID 查询客户端，不存在则抛出异常。 */
    public ClientApp getByIdOrThrow(Long id) {
        ClientApp app = clientAppMapper.selectById(id);
        if (app == null) {
            throw new ClientAppNotFoundException(id);
        }
        return app;
    }
}
