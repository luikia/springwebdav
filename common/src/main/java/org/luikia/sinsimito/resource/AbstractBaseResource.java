package org.luikia.sinsimito.resource;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.CollectionResource;
import io.milton.resource.DigestResource;
import io.milton.resource.GetableResource;
import io.milton.resource.PropFindableResource;
import io.milton.servlet.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractBaseResource implements DigestResource, PropFindableResource {

    private Map<String, String> authMap = new HashMap<>();

    protected Config config;

    public AbstractBaseResource(Config config) {
        this.config = config;
        String auth = config.getInitParameter("auth");
        String[] ups = StringUtils.splitByWholeSeparator(auth, ",");
        for (String up : ups) {
            String[] upArr = StringUtils.splitByWholeSeparator(up, ":");
            if (upArr.length != 2) {
                continue;
            }
            this.authMap.put(StringUtils.trim(upArr[0]), StringUtils.trim(upArr[1]));
        }


    }

    @Override
    public Object authenticate(String user, String requestedPassword) {
        if (!this.authMap.containsKey(user)) {
            return null;
        }
        String password = this.authMap.get(user);
        if (StringUtils.equals(password, requestedPassword)) {
            return user;
        }
        return null;

    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        String user = digestRequest.getUser();
        if (this.authMap.containsKey(user)) {
            DigestGenerator gen = new DigestGenerator();
            String password = this.authMap.get(user);
            String actual = gen.generateDigest(digestRequest, password);
            if (StringUtils.equals(actual, digestRequest.getResponseDigest())) {
                return digestRequest.getUser();
            } else {
                log.warn("that password is incorrect. Try 'password'");
            }
        } else {
            log.warn("user not found: " + digestRequest.getUser() + " - try 'user'");
        }
        return null;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        log.debug("authorise");
        return auth != null;
    }

    @Override
    public String getRealm() {
        return "testrealm@host.com";
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    protected abstract Class<? extends CollectionResource> directoryResourceClass();

    protected abstract Class<? extends GetableResource> fileResourceClass();

    protected boolean validateDirectoryResourceType(CollectionResource collectionResource) {
        return this.directoryResourceClass().isInstance(collectionResource);
    }
}
