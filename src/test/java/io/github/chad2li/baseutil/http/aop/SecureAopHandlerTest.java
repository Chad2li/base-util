package io.github.chad2li.baseutil.http.aop;

import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.impl.AppException;
import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.github.chad2li.baseutil.http.aop.service.IHeaderService;
import io.github.chad2li.baseutil.http.aop.service.ISecureService;
import io.github.chad2li.baseutil.http.filter.log.BufferedRequestWrapper;
import io.github.chad2li.baseutil.util.SpringUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import lombok.Data;
import org.apache.catalina.connector.Request;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpringUtils.class)
public class SecureAopHandlerTest {
    private IHeaderService<HeaderParamImpl> headerService;
    private ISecureService secureService;
    private SecureAopHandler<HeaderParamImpl> secureAopHandler;
    private HeaderParamImpl header;
    private final int timestampTimeoutseconds = 10;
    private Request request;

    @Before
    public void before() throws IOException {
        header = Mockito.mock(HeaderParamImpl.class);
        headerService = Mockito.mock(HeaderServiceImpl.class);
        secureService = Mockito.mock(SecureServiceImpl.class);
        secureAopHandler = new SecureAopHandler<>(headerService, secureService, timestampTimeoutseconds);
        request = Mockito.mock(Request.class);
    }

    @Test
    public void handler() {
        String requestId = System.currentTimeMillis() + "";
        String imei = "imei";
        String fullRequestId = imei + "-" + requestId;
        // request
        PowerMockito.mockStatic(SpringUtils.class);
        Mockito.when(SpringUtils.getRequest()).thenReturn(request);
        Mockito.when(request.getRequestURI()).thenReturn("abc");
        // header
        Mockito.when(headerService.cache()).thenReturn(header);
        Mockito.when(header.getTimestamp()).thenReturn(System.currentTimeMillis());
        Mockito.when(secureService.exists(Mockito.anyString())).thenReturn(false);
        Mockito.when(secureService.fullRequestId(Mockito.any())).thenReturn(fullRequestId);

        secureAopHandler.handler(null);
        Mockito.verify(secureService).cache(fullRequestId, timestampTimeoutseconds + 10);

        // timestamp timeout
        Mockito.when(header.getTimestamp()).thenReturn(System.currentTimeMillis() - 100 * 1000);
        try {
            secureAopHandler.handler(null);
        } catch (AppException e){
            Assert.assertEquals(IAppCode.fullCode(BaseCode.TIMESTAMP_TIMEOUT), e.getCode());
        }

        // request id duplicate
        Mockito.when(header.getTimestamp()).thenReturn(System.currentTimeMillis());
        Mockito.when(secureService.exists(Mockito.anyString())).thenReturn(true);
        try {
            secureAopHandler.handler(null);
        } catch (AppException e){
            Assert.assertEquals(IAppCode.fullCode(BaseCode.REQUESTID_DUPLICATE), e.getCode());
        }
    }

    public static class HttpRequestImpl extends BufferedRequestWrapper {
        public HttpRequestImpl(HttpServletRequest request) throws IOException {
            super(request);
        }
    }

    public static class SecureServiceImpl implements ISecureService {
        @Override
        public String fullRequestId(IHeaderService.AHeaderParam header) {
            return null;
        }

        @Override
        public boolean exists(String fullRequestId) {
            return false;
        }

        @Override
        public void cache(String fullRequestId, int expireSeconds) {

        }
    }

    public static class HeaderServiceImpl implements IHeaderService {
        @Override
        public void cache(AHeaderParam headerParam) {

        }

        @Override
        public AHeaderParam cache() {
            return null;
        }

        @Override
        public boolean hasUserId() {
            return false;
        }
    }

    @Data
    public static class HeaderParamImpl extends IHeaderService.AHeaderParam {
        private String imei;

        @Override
        public void parse(HttpServletRequest req) {
            imei = req.getHeader("imei");
        }

        @Override
        public String check(StringBuilder errMsg) {
            if (StringUtils.isNull(imei))
                return "parm invalid: imei";
            return null;
        }
    }
}