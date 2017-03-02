package com.zalora.aloha.hook;

import com.zalora.aloha.manager.CacheManager;
import javax.management.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Aspect
@Component
public class ShutdownHook {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MBeanServer mbs;

    @Before("execution(* org.springframework.boot.actuate.endpoint.ShutdownEndpoint.invoke())")
    public void beforeShutdown() {
        cacheManager.getPrimaryCache().stop();
        log.warn("Stopped Primary Cache");

        cacheManager.getSecondaryCache().stop();
        log.warn("Stopped Secondary Cache");

        cacheManager.getReadthroughCache().stop();
        log.warn("Stopped ReadThrough Cache");

        try {
            log.info("Rebalancing status: {}", (String) mbs.getAttribute(new ObjectName(
                "org.infinispan:component=StateTransferManager,manager=\"DefaultCacheManager\",name=\"main(dist_async)\",type=Cache"
                ), "rebalancingStatus"
            ));
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }

        cacheManager.getEmbeddedCacheManager().stop();
        log.warn("Stopped Cache Manager");

        log.warn("Stopped Cache Manager");
    }

}
