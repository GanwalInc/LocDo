package com.ganwal.locationTodoService;

import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.appengine.repackaged.com.google.api.client.http.javanet.NetHttpTransport;
import com.google.appengine.repackaged.com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@LoggedIn
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(AuthenticationFilter.class.getSimpleName());
    //private static String CLIENT_ID = "678353159738-khdjckjtbfoi9ni1j5lhh986k95jbk94.apps.googleusercontent.com";//app web client_id
    private static String CLIENT_ID = "678353159738-fh40t1mmcus2s2vimreau0194dg3bmci.apps.googleusercontent.com";//app web client_id
    public static final String AUTH_HEADER_PREFIX  = "Bearer ";
    public static final String GID_PATH_PARAM  = "gId";
    public static final String USERID_PATH_PARAM  = "id";




    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        log.fine("Request auth header:"+header);
        if(header == null || header.length() < 1 || !header.startsWith(AUTH_HEADER_PREFIX)) {
            throw new NotAuthorizedException("Unable to find Id Token in request");
        }
        String idToken = header.substring(AUTH_HEADER_PREFIX.length(), header.length());
        log.fine("idToken:"+idToken);
        if(idToken == null || idToken.length() < 1) {
            throw new NotAuthorizedException("Invalid Id Token in request");
        }
        //first verify the token
        String verifiedGId = verifyToken(idToken);
        log.fine("Returned gId after verifying:"+verifiedGId);
        if(verifiedGId == null || verifiedGId.trim().length() < 1) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        /*
        To make sure that user is trying to access its own data
        we get the user id from request, there are different types of requests
        for findUsers we look for user by its google id request param
        in other cases we will have the webservice user id as the path parameter
        there will be only one case where we don't have user id as path or request parm createUser,
        in that case we can look at the request body but then we have to recreate the body that
        will be more work, in createUser will only check for valid token
        */

        MultivaluedMap<String, String> queryParamMap = requestContext.getUriInfo().getQueryParameters();
        MultivaluedMap<String, String> pathParamMap = requestContext.getUriInfo().getPathParameters();
        String googleId = null;
        log.fine("queryParamMap:"+queryParamMap);
        if(queryParamMap != null && queryParamMap.get(GID_PATH_PARAM) != null) {
            log.fine("Found query params");
            List<String> requestParams = queryParamMap.get(GID_PATH_PARAM);
            if(requestParams != null) {
                googleId = requestParams.get(0);
                log.fine("Found googleId:"+googleId);
            }
        } else  if(pathParamMap != null && pathParamMap.getFirst(USERID_PATH_PARAM) != null) {
            log.fine("Found path params");
            String userIdStr = pathParamMap.getFirst(USERID_PATH_PARAM);
            log.fine("userIdStr from path param:"+userIdStr);
            if(userIdStr != null) {
                LocationTodoDao todoDao = new LocationTodoDao();
                User user = todoDao.getUserById(Long.parseLong(userIdStr));
                if(user != null) {
                    googleId = user.getGoogleId();
                    log.fine("Found googleId:"+googleId);
                }
            }
        }
        log.fine("Found googleId:"+googleId);
        log.fine("Found verifiedGId:"+verifiedGId);
        if(googleId != null && !userMatched(verifiedGId, googleId)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private String verifyToken(String idToken) {
        log.fine("idToken:"+idToken);
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Arrays.asList(CLIENT_ID))
                .setIssuer("https://accounts.google.com")
                .build();
        log.fine("verifier:"+verifier);
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            log.fine("googleIdToken:"+googleIdToken);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                String userGId = payload.getSubject();
                log.fine("User ID:" + userGId);
                String email = payload.getEmail();
                log.fine("email:" + email);
                String name = (String) payload.get("name");
                log.fine("name:" + name);
                return userGId;
            }
        } catch (GeneralSecurityException e) {
            log.severe("GeneralSecurityException verifying user token:"+e.getMessage());
        } catch (IOException e) {
            log.severe("IOException verifying user token:"+e.getMessage());
        }
        return null;
    }

    private boolean userMatched(String verifiedGId, String clientGId)  {
        if(clientGId != null && verifiedGId.equalsIgnoreCase(clientGId)) {
            log.fine("Google Id matched...its the same person...YAY");
            return true;
        }
        return false;
    }
}


