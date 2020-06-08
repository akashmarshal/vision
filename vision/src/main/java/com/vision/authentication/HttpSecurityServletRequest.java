package com.vision.authentication;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import jespa.ntlm.NtlmSecurityProvider;
import jespa.security.Account;
import jespa.security.ChainSecurityProvider;
import jespa.security.SecurityPrincipal;
import jespa.security.SecurityProvider;
import jespa.security.SecurityProviderException;
import jespa.util.LogStream;

public class HttpSecurityServletRequest extends HttpServletRequestWrapper
{
  SecurityFilter service;
  SecurityProvider provider;

  public HttpSecurityServletRequest(HttpServletRequest req, SecurityFilter service, SecurityProvider provider)
  {
    super(req);
    this.service = service;
    this.provider = provider;
  }

  public SecurityProvider getSecurityProvider()
  {
    if ((this.provider instanceof ChainSecurityProvider)) {
      ChainSecurityProvider csp = (ChainSecurityProvider)this.provider;
      try {
        return csp.getSecurityProvider();
      }
      catch (SecurityProviderException spe) {
        if (LogStream.level >= 1) {
          spe.printStackTrace(SecurityFilter.log);
        }
        return null;
      }
    }
    return this.provider;
  }

  public String getAuthType()
  {
    if ((this.provider instanceof NtlmSecurityProvider))
      try {
        if (this.provider.getAccount(null, null) != null)
          return "NTLM";
      }
      catch (SecurityProviderException spe) {
      }
    return null;
  }

  public String getRemoteUser()
  {
    try
    {
      return this.provider.getIdentity();
    } catch (SecurityProviderException spe) {
      if (LogStream.level >= 1)
        spe.printStackTrace(SecurityFilter.log);
    }
    return null;
  }

  public Principal getUserPrincipal()
  {
    try
    {
      String ident = this.provider.getIdentity();
      if (ident != null)
        return new SecurityPrincipal(ident);
    } catch (SecurityProviderException spe) {
      if (LogStream.level >= 1)
        spe.printStackTrace(SecurityFilter.log);
    }
    return null;
  }

  public boolean isUserInRole(String role)
  {
    boolean ret = false;
    try
    {
      Account account = this.provider.getAccount(null, null);
      if (account != null) {
        int size = account.size();
        ret = account.isMemberOf(role);
        if (account.size() > size)
        {
          if (LogStream.level >= 4) {
        	  SecurityFilter.log.println("HttpSecurityServletRequest: Account modified by isMemberOf");
          }
          this.service.storeProviderState(getSession(true), this.provider, this.service._getConnectionId(this));
        }
      }
    } catch (SecurityProviderException spe) {
      if (LogStream.level >= 1) {
        spe.printStackTrace(SecurityFilter.log);
      }
    }
    return ret;
  }
}