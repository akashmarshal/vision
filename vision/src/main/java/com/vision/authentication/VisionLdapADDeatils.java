package com.vision.authentication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.stereotype.Component;

import com.vision.util.ValidationUtil;
import com.vision.vb.AdUserVb;

@Component
public class VisionLdapADDeatils {
	

	public AdUserVb getSearchResult(String userLoginId, String adProperty) throws NamingException{
		
		String username = "Infopool.Service";
        String password = "Uba123456";
	    String base = "cn=Users,DC=ubagroup,DC=com";
	    String dn = username+"@ubagroup.com"; //"cn=" + username + "," + base;
	    String ipAddress = "ldapserver";//the active directory server ip address
	    String ldapURL = "ldap://" + ipAddress;//389 is default port for LDAP
	     
	    
	    // Setup environment for authenticating
	    Hashtable<String, String> environment =   new Hashtable<String, String>();
	    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	    environment.put(Context.PROVIDER_URL, ldapURL);
	    environment.put(Context.SECURITY_AUTHENTICATION, "simple");
	    environment.put(Context.SECURITY_PRINCIPAL, dn);
	    environment.put(Context.SECURITY_CREDENTIALS, password);
		
	    DirContext dirContext = new InitialDirContext(environment);
	    /*DirContext dirContext;
	    try {
	    	dirContext = new InitialDirContext(environment);
	    } catch (NamingException e) {
	         throw new RuntimeException(e);
	    }*/
//        String searchFilter = "sAMAccountName=Ousmane.Leye";
	    if(!ValidationUtil.isValid(adProperty)){
	    	adProperty = "sAMAccountName";
	    }
	    String searchFilter = adProperty+"="+userLoginId;
        String searchBase = "DC=ubagroup,DC=com";
	    
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        AdUserVb adUser = new AdUserVb();
		String groupsReturnedAtts[]={"givenName","distinguishedName", "initials","sn","userPrincipalName","sAMAccountName","displayName","name","description",
				"physicalDeliveryOfficeName","telephoneNumber","mail","wWWHomePage","password","streetAddress","postOfficeBox","l","st","postalCode","co","c","countryCode",
				"memberOf","accountExpires","userAccountControl","profilePath","scriptPath","homeDirectory","homeDrive","userWorkstations","homePhone","pager","mobile",
				"facsimileTelephoneNumber","ipPhone","info","title","department","company","manager","mailNickName","displayNamePrintable",
				"msExchHideFromAddressLists","submissionContLength","delivContLength","msExchRequireAuthToSendTo","unauthOrig","authOrig","publicDelegates",
				"altRecipient","deliverAndRedirect","msExchRecipLimit","mDBStorageQuota","mDBOverQuotaLimit","mDBOverHardQuotaLimit","deletedItemFlags",
				"garbageCollPeriod","msExchOmaAdminWirelessEnable","protocolSettings","tsAllowLogon","tsProfilePath","tsHomeDir","tsHomeDirDrive","tsInheritInitialProgram",
				"tsIntialProgram","tsWorkingDir","tsDeviceClientDrives","tsDeviceClientPrinters","tsDeviceClientDefaultPrinter","tsTimeOutSettingsDisConnections",
				"tsTimeOutSettingsConnections","tsTimeOutSettingsIdle","tsBrokenTimeOutSettings","tsReConnectSettings","tsShadowSettings","preventDeletion",
				"managerCanUpdateMembers","primaryGroupID","msExchAdminGroup","msExchHomeServerName","managedBy","targetAddress","proxyAddresses","msExchPoliciesExcluded",
				"GroupMemberObjectId","LitigationHoldEnabled","LitigationHoldDuration","InPlaceArchive","ArchiveName","O365userPrincipalName"};
		constraints.setReturningAttributes(groupsReturnedAtts);
        
//        System.out.println("** Search Starts : "  + System.currentTimeMillis());
        final NamingEnumeration<?> searchResults = dirContext.search(searchBase,searchFilter,constraints);
//        System.out.println("** Search Ends : "  + System.currentTimeMillis());
        
        if(searchResults != null && searchResults.hasMore()){
            // For Example , displayed attribute values
            final SearchResult searchResult = (SearchResult)searchResults.next();
            String strMemberOf = String.valueOf(searchResult.getAttributes().get("memberOf"));
            for (NamingEnumeration enums = searchResult.getAttributes().getAll(); enums.hasMore();) {
                final Attribute attribute = (Attribute)enums.next();
                String adAttributeId = (attribute.getID().substring(0, 1).toUpperCase())+""+attribute.getID().substring(1, attribute.getID().length());
                String adAttributeValue = String.valueOf((searchResult.getAttributes().get(adAttributeId) == null? "" : searchResult.getAttributes().get(adAttributeId) ));
                adAttributeValue = adAttributeValue.substring(adAttributeValue.indexOf(":")+1, adAttributeValue.length());
//                System.out.println(""+adAttributeId+" -> Value ["+adAttributeValue+"]");
//                System.out.println("Attribute Name -> " + attribute.getID());
                invokeMethod("set"+adAttributeId, adUser, adAttributeValue.trim());
            }
/*            strMemberOf = strMemberOf.replaceAll("DC=ubagroup,DC=com", "").replaceAll("OU=Groups", "").replaceAll("CN=", "").replaceAll("", "");
            String[] strMemberOfArray = strMemberOf.split(",");*/
        }
 
        return adUser;
    }
	private static void invokeMethod(String methodName,Object object, String value){
		Class[] paramString = new Class[1];	
		paramString[0] = String.class;
		try {
			Method m = AdUserVb.class.getMethod(methodName, paramString);
			m.setAccessible(true);
			m.invoke(object, value);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
