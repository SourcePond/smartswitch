package ch.sourcepond.commons.smartswitch.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.*;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

/**
 * Created by rolandhauser on 29.12.16.
 */
@SuppressWarnings("unchecked")
public class DefaultInvocationHandlerTest {
    private final Bundle bundle = mock(Bundle.class);
    private final BundleContext context = mock(BundleContext.class);
    private final ServiceReference<ExecutorService> serviceReference = mock(ServiceReference.class);
    private final ServiceReference<ExecutorService> secondServiceReference = mock(ServiceReference.class);
    private final ExecutorService defaultService = mock(ExecutorService.class);
    private final ExecutorService service = mock(ExecutorService.class);
    private final ExecutorService secondService = mock(ExecutorService.class);
    private final Supplier<ExecutorService> supplier = mock(Supplier.class);
    private final Consumer<ExecutorService> serviceAvailableHook = mock(Consumer.class);
    private final DefaultInvocationHandler<ExecutorService> defaultInvocationHandler = new DefaultInvocationHandler<>(supplier, serviceAvailableHook);

    private ExecutorService createProxy() {
        return (ExecutorService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ExecutorService.class}, defaultInvocationHandler);
    }

    @Before
    public void setup() {
        when(bundle.getBundleContext()).thenReturn(context);
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(secondServiceReference.getBundle()).thenReturn(bundle);
    }

    @Test
    public void initService_NoMatchingServiceAvailable() {
        when(supplier.get()).thenReturn(defaultService);
        defaultInvocationHandler.initService(null);
        createProxy().shutdown();
        verify(defaultService).shutdown();
    }

    @Test
    public void initService_MatchingReferenceFoundButServiceDegistered() {
        when(supplier.get()).thenReturn(defaultService);
        defaultInvocationHandler.initService(serviceReference);
        createProxy().shutdown();
        verify(defaultService).shutdown();
    }

    @Test
    public void initService_MatchingServiceFound() {
        when(context.getService(serviceReference)).thenReturn(service);
        defaultInvocationHandler.initService(serviceReference);
        createProxy().shutdown();
        verify(service).shutdown();
    }

    @Test
    public void replaceDefaultService_NoRankingSpecified() {
        when(serviceReference.getProperty(Constants.SERVICE_RANKING)).thenReturn(-1);
        initService_NoMatchingServiceAvailable();

        when(context.getService(secondServiceReference)).thenReturn(secondService);
        final ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, secondServiceReference);
        defaultInvocationHandler.serviceChanged(event);

        // The hook should be called because the previous service is
        // a default service.
        verify(serviceAvailableHook).accept(defaultService);
        createProxy().shutdown();
        verify(secondService).shutdown();
    }

    @Test
    public void replaceDefaultService_NoRankingSpecified_IllegalTyoe() {
        when(serviceReference.getProperty(Constants.SERVICE_RANKING)).thenReturn(-1);
        initService_NoMatchingServiceAvailable();

        when(secondServiceReference.getProperty(Constants.SERVICE_RANKING)).thenReturn("IllegalValue");
        when(context.getService(secondServiceReference)).thenReturn(secondService);
        final ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, secondServiceReference);
        defaultInvocationHandler.serviceChanged(event);

        // The hook should be called because the previous service is
        // a default service.
        verify(serviceAvailableHook).accept(defaultService);
        createProxy().shutdown();
        verify(secondService).shutdown();
    }

    @Test
    public void serviceRegistered_NoRankingSpecified() {
        when(serviceReference.getProperty(Constants.SERVICE_RANKING)).thenReturn(-1);
        initService_MatchingServiceFound();

        when(context.getService(secondServiceReference)).thenReturn(secondService);
        final ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, secondServiceReference);
        defaultInvocationHandler.serviceChanged(event);

        // The hook should never be called because the previous service is
        // an OSGi service as well.
        verify(serviceAvailableHook, never()).accept(any());
        createProxy().shutdown();
        verify(secondService).shutdown();
    }

    @Test
    public void serviceRegistered_NoRankingSpecified_IllegalTyoe() {
        when(serviceReference.getProperty(Constants.SERVICE_RANKING)).thenReturn(-1);
        initService_MatchingServiceFound();

        when(secondServiceReference.getProperty(Constants.SERVICE_RANKING)).thenReturn("IllegalValue");
        when(context.getService(secondServiceReference)).thenReturn(secondService);
        final ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, secondServiceReference);
        defaultInvocationHandler.serviceChanged(event);

        // The hook should never be called because the previous service is
        // an OSGi service as well.
        verify(serviceAvailableHook, never()).accept(any());
        createProxy().shutdown();
        verify(secondService).shutdown();
    }

    @Test
    public void serviceUnregistered_NotInterested() {
        initService_MatchingServiceFound();
        defaultInvocationHandler.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, secondServiceReference));

        // Should be still the previous service
        createProxy().shutdown();
        verify(service, times(2)).shutdown();
    }

    @Test
    public void serviceUnregistered() {
        when(supplier.get()).thenReturn(defaultService);
        initService_MatchingServiceFound();
        defaultInvocationHandler.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, serviceReference));

        // Should be still the previous service
        createProxy().shutdown();
        verify(defaultService).shutdown();
    }
}
