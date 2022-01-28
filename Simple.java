package org.chaostocosmos.leap.http;

import java.io.File;
import java.net.http.HttpClient;

public class Simple {
    public static void sendToServer(ExporterServerVO vo) throws Exception {
		Logger.getInstance().debug("@@@@@@@@@@@ Deploy VO: "+vo.toString() );
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
		client.getParams().setAuthenticationPreemptive(true);
		Credentials creds = new UsernamePasswordCredentials(vo.getId(), vo.getPasswd());
		client.getState().setCredentials(AuthScope.ANY, creds);
		client.getState().setProxyCredentials(AuthScope.ANY, creds);
		
		DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
		retryhandler.setRetryCount(0);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
		
		PostMethod postMethod = new PostMethod(vo.getHost()+":"+vo.getPort()+vo.getJobPath());
        Logger.getInstance().debug("REQUEST: "+vo.getHost()+":"+vo.getPort()+vo.getJobPath());
        try  {
            File fileToUpload = new File(vo.getAbsoluteFilePath());
            Part[] parts = { new FilePart("file", fileToUpload) };
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
			
    		int status = client.executeMethod(postMethod);     		
    		if (status != 200) {
    			throw new Exception("FAIL CODE: "+status+"   FAIL MESSAGE: "+postMethod.getStatusText());
    		} else {
    			Logger.getInstance().debug("[status]" + status + "[response]" + postMethod.getStatusText());
    		}
        } catch (Exception e) {
        	try {
	    		String msg = postMethod.getStatusText();
	    		throw new Exception("FAIL: "+msg);
        	} catch(Exception e1) {
        		throw new Exception("FAIL: Deploying job was failed. Please check request configurations.");
        	}
        } finally {
            postMethod.releaseConnection();
        }
	}    
}
