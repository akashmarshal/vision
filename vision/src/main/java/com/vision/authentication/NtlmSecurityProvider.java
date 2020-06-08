/* Copyright (c) 2010, IOPLEX Software
 * 
 * All source, binaries and materials in this package are protected by the
 * EULA in the LICENSE.txt file in the top level directory of this package
 * unless explicitly stated otherwise within individual source files. The
 * following license applies to this source file only.
 * 
 * Copyright (c) 2010, IOPLEX Software
 *
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 * 
 *   * Neither the name of IOPLEX Software nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.vision.authentication;

import java.util.Map;

import jespa.security.PasswordCredential;
import jespa.security.SecurityPrincipal;
import jespa.security.Domain;
import jespa.security.Account;
import jespa.security.SecurityProviderException;
import jespa.security.Properties;
import jespa.ntlm.NtlmResponse;
import jespa.ntlm.NtlmDomain;

public class NtlmSecurityProvider extends  jespa.ntlm.NtlmSecurityProvider
{

	private static final long serialVersionUID = 1L;

	public class MyAccount extends Properties implements Account
    {

        NtlmSecurityProvider provider;

        MyAccount(NtlmSecurityProvider provider)
        {
            this.provider = provider;
        }

        public boolean isMemberOf(String group) throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void create(String[] attrs) throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void create() throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void update(String[] attrs) throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void update() throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void delete() throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void setPassword(char[] password) throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
        public void changePassword(char[] oldpassword, char[] newpassword) throws SecurityProviderException
        {
            throw new SecurityProviderException(0, "Not implemented");
        }
    }

    MyAccount account;

    public NtlmSecurityProvider(Map properties) {
        super(properties);
    }
    @Override
    public Account getAccount(String acctname, String[] attrs) throws SecurityProviderException
    {
        if (acctname != null && (attrs != null || attrs.length != 0))
            throw new SecurityProviderException(0, "Currently not implemented, acctname and attrs must be null or empty");
        return account;
    }
    @Override
    public void authenticate(Object credential) throws SecurityProviderException
    {
        /* This example is designed to illustrate how to create a custom NTLM
         * authentication authority such as one that would allow NTLM HTTP Single
         * Sign-On on top of an SQL database. In this particular example, we simply
         * get one set of credentials from the provider properties.
         * Note that the plaintext password is required. A hash like those
         * commonly stored in LDAP could not be used.
         */
        String nbtName = (String)getProperty("domain.netbios.name");
        String dnsName = (String)getProperty("domain.dns.name");
        String myusername = (String)getProperty("my.username");
        String mypassword = (String)getProperty("my.password");

        if (credential instanceof NtlmResponse) {
            NtlmResponse resp = (NtlmResponse)credential;
            String domain = resp.getDomain();
            String username = resp.getUsername();

            if (domain == null || domain.trim().length() == 0) {
                domain = nbtName;
            }

            if (domain.equalsIgnoreCase(nbtName) || domain.equalsIgnoreCase(dnsName)) {
                /* If the domain was not supplied or matches our domain, we are the
                 * authority for the account.
                 */
                if (username.equalsIgnoreCase(myusername)) {
                    /* Build another NtlmResponse object with the raw credentials and then
                     * directly compare it with the one supplied by the client.
                     */

                    NtlmResponse local = new NtlmResponse(resp,
                                domain,
                                myusername,
                                mypassword.toCharArray(),
                                getTargetInformation());
                    if (resp.equals(local)) {
                        account = new MyAccount(this);
                        account.put("sAMAccountName", myusername);
                        return; // SUCCESS
                    }

                    throw new SecurityProviderException(SecurityProviderException.STATUS_INVALID_CREDENTIALS,
                            "Invalid credentials for " + domain + '\\' + username);
                }
            }
            throw new SecurityProviderException(SecurityProviderException.STATUS_ACCOUNT_NOT_FOUND,
                    "Account not found for " + domain + '\\' + username);
        } else if (credential instanceof PasswordCredential) {
            PasswordCredential cred = (PasswordCredential)credential;
            SecurityPrincipal princ = cred.getSecurityPrincipal();
            String domain = princ.getDomain();
            String username = princ.getUsername();

            if (domain == null ||
                        domain.trim().length() == 0 ||
                        domain.equalsIgnoreCase(nbtName) ||
                        domain.equalsIgnoreCase(dnsName)) {
                if (username.equalsIgnoreCase(myusername)) {
                    String password = new String(cred.getPassword());
                    if (password.equals(mypassword)) {
                        /* Hard code canonicalization for now since the code to canonicalize
                         * a name is somewhat long winded and a future release should
                         * provide a relatively simple way to do it.
                         *
                         * We do not need to canonicalize for an NtlmResponse object because
                         * that is supplied through NtlmSecurityProvider.acceptSecContext
                         * which installs the identity.
                         */
                        identity = nbtName + "\\" + username;
                        account = new MyAccount(this);
                        account.put("sAMAccountName", username);
                        return; // SUCCESS
                    }

                    throw new SecurityProviderException(SecurityProviderException.STATUS_INVALID_CREDENTIALS,
                            "Invalid credentials for " + domain + '\\' + username);
                }
            }

            throw new SecurityProviderException(SecurityProviderException.STATUS_ACCOUNT_NOT_FOUND,
                    "Account not found for " + domain + '\\' + username);
        } else {
            throw new SecurityProviderException(0, "Unsupported credential type");
        }
        /* If this provider is not an authority for the supplied credential or
         * it cannot handle the supplied credential, it is easy to simply call
         * the parent provider to try to validate it (of course you'd have to
         * reorganize the above exceptions as well).
         */
//      super.authenticate(credential);
    }
    @Override
    public Domain getDomain(String dname, String[] attrs) throws SecurityProviderException
    {
        if (attrs != null)
            throw new SecurityProviderException(0, "Currently not supported, attrs must be null");

        String nbtName = (String)get("domain.netbios.name");
        String dnsName = (String)get("domain.dns.name");
        if (nbtName == null && dnsName == null)
            throw new SecurityProviderException(0, "domain.netbios.name and / or domain.dns.name properties are required");

        if (dname == null || dname.equalsIgnoreCase(nbtName) || dname.equalsIgnoreCase(dnsName)) {
            Domain ret = new NtlmDomain();
            if (nbtName != null)
                ret.put("domain.netbios.name", nbtName);
            if (dnsName != null)
                ret.put("domain.dns.name", dnsName);
            return ret;
        }

        throw new SecurityProviderException(0, "Domain not found: " + dname);
//      return super.getDomain(dname);
    }
}
