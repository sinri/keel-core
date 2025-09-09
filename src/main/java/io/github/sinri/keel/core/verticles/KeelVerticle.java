package io.github.sinri.keel.core.verticles;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * An extension of the {@link Verticle} interface, providing additional functionality and convenience methods.
 * This interface is designed to be implemented by verticles that are part of the Keel framework.
 *
 * @since 3.2.0
 */
public interface KeelVerticle extends Verticle {

    /**
     * Creates a new instance of {@link KeelVerticle} that wraps the provided start future supplier.
     *
     * @param startFutureSupplier a supplier that provides a future which represents the start operation of the verticle
     * @return a new instance of {@link KeelVerticle} wrapping the provided start future supplier
     * @since 4.0.2
     */
    static KeelVerticle instant(@Nonnull Supplier<Future<Void>> startFutureSupplier) {
        return new KeelVerticleWrap(startFutureSupplier);
    }

    /**
     * Creates a new instance of {@link KeelVerticle} that wraps the provided starter function.
     * The starter function accepts a Promise that can be used to trigger the verticle's undeployment.
     *
     * @param starter a function that takes a stop promise and returns a future representing the start operation
     * @return a new instance of {@link KeelVerticle} wrapping the provided starter function
     * @since 4.0.12
     */
    static KeelVerticle instant(@Nonnull Function<Promise<Void>, Future<Void>> starter) {
        return new KeelVerticleWrap(starter);
    }

    /**
     * Retrieves the threading model associated with the current execution context of the verticle.
     *
     * @return the threading model of the current context
     * @since 4.1.3
     */
    ThreadingModel getContextThreadModal();

    /**
     * Returns the unique identifier for the deployment of this verticle.
     *
     * @return the deployment ID as a string
     * @since 2.8
     */
    String deploymentID();


    /**
     * Retrieves the configuration for this verticle.
     *
     * @return a {@link JsonObject} containing the configuration settings for the verticle
     */
    JsonObject config();

    /**
     * Returns a JSON object containing information about the verticle.
     *
     * @return a {@link JsonObject} with the following fields:
     *         <ul>
     *             <li>class: the fully qualified name of the class implementing this verticle</li>
     *             <li>config: the configuration settings for the verticle as a {@link JsonObject}</li>
     *             <li>deployment_id: the unique identifier for the deployment of this verticle</li>
     *         </ul>
     */
    default JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID());
    }

    /**
     * Deploys the current verticle with the specified deployment options.
     *
     * @param deploymentOptions the options to use for deploying the verticle
     * @return a future that completes with the deployment ID if the deployment is successful, or fails with an
     *         exception if the deployment fails
     */
    default Future<String> deployMe(DeploymentOptions deploymentOptions) {
        return Keel.getVertx().deployVerticle(this, deploymentOptions);
    }

    /**
     * Undeploy the current verticle from the Vert.x instance.
     *
     * @return a future that completes when the undeployment is successful, or fails with an exception if the
     *         undeployment fails
     * @since 2.8 add default implementation
     */
    default Future<Void> undeployMe() {
        return Keel.getVertx().undeploy(deploymentID());
    }
}
