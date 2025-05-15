package com.letsvpn.user.service;

import com.letsvpn.user.dto.NodeRegistrationRequest;
import com.letsvpn.user.dto.NodeUpdateRequest;
import com.letsvpn.user.entity.Node;

public interface NodeService {
    Node createNode(NodeRegistrationRequest request);
    Node updateNode(Long nodeId, NodeUpdateRequest request,String requestIp);
}