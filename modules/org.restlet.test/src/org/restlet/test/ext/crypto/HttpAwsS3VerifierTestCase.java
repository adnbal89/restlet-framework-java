package org.restlet.test.ext.crypto;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.crypto.internal.AwsUtils;
import org.restlet.ext.crypto.internal.AwsVerifier;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Verifier;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for {@link AwsVerifier}.
 * 
 * @author Jean-Philippe Steinmetz <caskater47@gmail.com>
 */
public class HttpAwsS3VerifierTestCase extends RestletTestCase {
    private static final String ACCESS_ID = "0PN5J17HBGZHT7JJ3X82";

    private static final String ACCESS_KEY = "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o";

    private static final String ATTRIBUTES_HEADERS = "org.restlet.http.headers";

    private AwsVerifier awsVerifier;

    private LocalVerifier localVerifier;

    private Request createRequest() {
        Request request = new Request();
        Form headers = new Form();
        request.getAttributes().put(ATTRIBUTES_HEADERS, headers);
        request.setMethod(Method.GET);
        request.setResourceRef("http://johnsmith.s3.amazonaws.com/photos/puppy.jpg");

        return request;
    }

    @Before
    public void setUp() throws Exception {
        localVerifier = new LocalVerifier() {
            @Override
            public char[] getLocalSecret(String identifier) {
                if (ACCESS_ID.equals(identifier))
                    return ACCESS_KEY.toCharArray();
                else
                    return "password".toCharArray();
            }
        };

        awsVerifier = new AwsVerifier(localVerifier);
    }

    @After
    public void tearDown() throws Exception {
        awsVerifier = null;
        localVerifier = null;
    }

    @Test
    public void testVerify() {
        Request request = createRequest();
        Form headers = (Form) request.getAttributes().get(ATTRIBUTES_HEADERS);

        // Test for missing due to no challenge response
        Assert.assertEquals(Verifier.RESULT_MISSING,
                awsVerifier.verify(request, null));

        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_AWS_S3);
        request.setChallengeResponse(cr);

        // Test missing due to no identifier
        Assert.assertEquals(Verifier.RESULT_MISSING,
                awsVerifier.verify(request, null));

        // Test authentication with bad credentials
        String sig = AwsUtils.getSignature(request,
                "badpassword".toCharArray());
        cr.setRawValue(ACCESS_ID + ":" + sig);
        Assert.assertEquals(Verifier.RESULT_INVALID,
                awsVerifier.verify(request, null));

        // Test authentication with valid credentials
        sig = AwsUtils.getSignature(request, ACCESS_KEY.toCharArray());
        cr.setRawValue(ACCESS_ID + ":" + sig);
        Assert.assertEquals(Verifier.RESULT_VALID,
                awsVerifier.verify(request, null));

        // Test invalid due to no date header
        headers.removeAll(HeaderConstants.HEADER_DATE);
        Assert.assertEquals(Verifier.RESULT_INVALID,
                awsVerifier.verify(request, null));

        // Test stale due to out of date header
        headers.add(HeaderConstants.HEADER_DATE,
                "Tue, 27 Mar 1999 19:36:42 +0000");
        Assert.assertEquals(Verifier.RESULT_STALE,
                awsVerifier.verify(request, null));
    }
}