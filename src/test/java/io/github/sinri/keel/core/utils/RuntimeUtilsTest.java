package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.runtime.CPUTimeResult;
import io.github.sinri.keel.core.utils.runtime.JVMMemoryResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RuntimeUtilsTest {
    @Test
    void getCPUTimeSnapshotReadsOshiProcessorTicks() {
        CPUTimeResult result = assertDoesNotThrow(RuntimeUtils::getCPUTimeSnapshot);

        assertTrue(result.statTime() > 0);
        assertTrue(result.spentInUserState() >= 0);
        assertTrue(result.spentInNiceState() >= 0);
        assertTrue(result.spentInSystemState() >= 0);
        assertTrue(result.spentInIdleState() >= 0);
        assertTrue(result.spentInIOWaitState() >= 0);
        assertTrue(result.spentInIRQState() >= 0);
        assertTrue(result.spentInSoftIRQState() >= 0);
        assertTrue(result.spentInStealState() >= 0);
        assertNotNull(result.cpuUsagePercent());
    }

    @Test
    void makeJVMMemorySnapshotReadsOshiGlobalMemory() {
        JVMMemoryResult result = assertDoesNotThrow(RuntimeUtils::makeJVMMemorySnapshot);

        assertTrue(result.statTime() > 0);
        assertTrue(result.physicalMaxBytes() > 0);
        assertTrue(result.physicalUsedBytes() >= 0);
        assertTrue(result.physicalUsedBytes() <= result.physicalMaxBytes());
        assertTrue(result.runtimeHeapMaxBytes() > 0);
        assertTrue(result.runtimeHeapAllocatedBytes() > 0);
        assertTrue(result.runtimeHeapUsedBytes() >= 0);
        assertTrue(result.mxHeapUsedBytes() >= 0);
        assertTrue(result.mxNonHeapUsedBytes() >= 0);
    }
}
