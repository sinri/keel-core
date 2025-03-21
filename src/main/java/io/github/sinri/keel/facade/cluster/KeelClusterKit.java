package io.github.sinri.keel.facade.cluster;

import com.hazelcast.config.*;
import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This interface provides a shortcut to create an instance of {@link ClusterManager} for SAE (Serverless Application
 * Environment).
 * It includes methods to obtain the cluster manager, and to retrieve the network address and ID of the Vert.x node.
 *
 * @since 3.0.0
 */
public interface KeelClusterKit {

    static ClusterManager createClusterManagerForSAE(
            @Nonnull String clusterName,
            @Nonnull List<String> members,
            int port, int portCount
    ) {
        TcpIpConfig tcpIpConfig = new TcpIpConfig()
                .setEnabled(true)
                .setConnectionTimeoutSeconds(1);
        members.forEach(tcpIpConfig::addMember);

        JoinConfig joinConfig = new JoinConfig()
                .setMulticastConfig(new MulticastConfig().setEnabled(false))
                .setTcpIpConfig(tcpIpConfig);

        NetworkConfig networkConfig = new NetworkConfig()
                .setJoin(joinConfig)
                .setPort(port)
                .setPortCount(portCount)
                .setPortAutoIncrement(portCount > 1)
                .setOutboundPorts(List.of(0));

        Config hazelcastConfig = ConfigUtil.loadConfig()
                                           .setClusterName(clusterName)
                                           .setNetworkConfig(networkConfig);

        return new HazelcastClusterManager(hazelcastConfig);
    }

    @TechnicalPreview(since = "3.1.0")
    @Nullable
    ClusterManager getClusterManager();

    @TechnicalPreview(since = "3.1.0")
    default @Nonnull String getVertxNodeNetAddress() {
        if (getClusterManager() == null) return "";
        NodeInfo nodeInfo = getClusterManager().getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    @TechnicalPreview(since = "3.1.0")
    default @Nonnull String getVertxNodeID() {
        if (getClusterManager() == null) return "";
        return getClusterManager().getNodeId();
    }
}
