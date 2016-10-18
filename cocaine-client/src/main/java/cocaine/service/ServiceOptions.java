package cocaine.service;

/**
 * @author metal
 */
public class ServiceOptions {
    public final long readTimeoutInMs;
    public final boolean immediatelyFlushAllInvocations;
    public final int maxNumberOfOpenChannels;

    public ServiceOptions(long readTimeoutInMs, boolean immediatelyFlushAllInvocations, int maxNumberOfOpenChannels) {
        this.readTimeoutInMs = readTimeoutInMs;
        this.immediatelyFlushAllInvocations = immediatelyFlushAllInvocations;
        this.maxNumberOfOpenChannels = maxNumberOfOpenChannels;
    }

    public ServiceOptions() {
        this.readTimeoutInMs = 0;
        this.immediatelyFlushAllInvocations = false;
        this.maxNumberOfOpenChannels = 1;
    }
}
