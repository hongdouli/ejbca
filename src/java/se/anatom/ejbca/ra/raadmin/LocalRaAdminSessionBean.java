package se.anatom.ejbca.ra.raadmin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import se.anatom.ejbca.BaseSessionBean;
import se.anatom.ejbca.SecConst;
import se.anatom.ejbca.authorization.AuthorizationDeniedException;
import se.anatom.ejbca.authorization.IAuthorizationSessionLocal;
import se.anatom.ejbca.authorization.IAuthorizationSessionLocalHome;
import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.log.ILogSessionLocal;
import se.anatom.ejbca.log.ILogSessionLocalHome;
import se.anatom.ejbca.log.LogEntry;

/**
 * Stores data used by web server clients.
 * Uses JNDI name for datasource as defined in env 'Datasource' in ejb-jar.xml.
 *
 * @version $Id: LocalRaAdminSessionBean.java,v 1.31 2003-09-04 14:36:14 herrvendil Exp $
 */
public class LocalRaAdminSessionBean extends BaseSessionBean  {

    private static Logger log = Logger.getLogger(LocalRaAdminSessionBean.class);

    /** Var holding JNDI name of datasource */
    private String dataSource = "";

    /** The home interface of  AdminPreferences entity bean */
    private AdminPreferencesDataLocalHome adminpreferenceshome=null;

    /** The home interface of  EndEntityProfileData entity bean */
    private EndEntityProfileDataLocalHome profiledatahome=null;
    
    /** The home interface of  GlobalConfiguration entity bean */
    private GlobalConfigurationDataLocalHome globalconfigurationhome = null;

    /** Var containing the global configuration. */
    private GlobalConfiguration globalconfiguration;

    /** The local interface of  log session bean */
    private ILogSessionLocal logsession = null;
    
    /** the local inteface of authorization session */
    private IAuthorizationSessionLocal authorizationsession = null;
    

    public final static String EMPTY_ENDENTITYPROFILENAME   = "EMPTY";

    private static final String DEFAULTUSERPREFERENCE = "default";
    
    /**
     * Default create for SessionBean without any creation Arguments.
     * @throws CreateException if bean instance can't be created
     */
    public void ejbCreate() throws CreateException {
        debug(">ejbCreate()");
      try{
        dataSource = (String)lookup("java:comp/env/DataSource", java.lang.String.class);
        debug("DataSource=" + dataSource);


        adminpreferenceshome = (AdminPreferencesDataLocalHome)lookup("java:comp/env/ejb/AdminPreferencesDataLocal", AdminPreferencesDataLocalHome.class);
        profiledatahome = (EndEntityProfileDataLocalHome)lookup("java:comp/env/ejb/EndEntityProfileDataLocal", EndEntityProfileDataLocalHome.class);
        globalconfigurationhome = (GlobalConfigurationDataLocalHome)lookup("java:comp/env/ejb/GlobalConfigurationDataLocal", GlobalConfigurationDataLocalHome.class);
        
        
        debug("<ejbCreate()");
      }catch(Exception e){
         throw new EJBException(e);
      }

    }


    /** Gets connection to Datasource used for manual SQL searches
     * @return Connection
     */
    private Connection getConnection() throws SQLException, NamingException {
        DataSource ds = (DataSource)getInitialContext().lookup(dataSource);
        return ds.getConnection();
    } //getConnection
    

    /** Gets connection to log session bean
     */
    private ILogSessionLocal getLogSession() {
        if(logsession == null){
          try{
            ILogSessionLocalHome logsessionhome = (ILogSessionLocalHome) lookup("java:comp/env/ejb/LogSessionLocal",ILogSessionLocalHome.class);
            logsession = logsessionhome.create();
          }catch(Exception e){
             throw new EJBException(e);
          }
        }
        return logsession;
    } //getLogSession
    

    /** Gets connection to authorization session bean
     * @return Connection
     */
    private IAuthorizationSessionLocal getAuthorizationSession() {
        if(authorizationsession == null){
          try{
            IAuthorizationSessionLocalHome authorizationsessionhome = (IAuthorizationSessionLocalHome) lookup("java:comp/env/ejb/AuthorizationSessionLocal",IAuthorizationSessionLocalHome.class);
            authorizationsession = authorizationsessionhome.create();
          }catch(Exception e){
             throw new EJBException(e);
          }
        }
        return authorizationsession;
    } //getAuthorizationSession    

    
 

     /**
     * Finds the admin preference belonging to a certificate serialnumber. Returns null if admin doesn't exists.
     */
    public AdminPreference getAdminPreference(Admin admin, String certificatefingerprint){
        debug(">getAdminPreference()");
        AdminPreference ret =null;
        try {
            AdminPreferencesDataLocal apdata = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
            ret = apdata.getAdminPreference();
        } catch (javax.ejb.FinderException fe) {
             // Create new configuration
             ret=null;
        } catch(Exception e){
          throw new EJBException(e);
        }
        debug("<getAdminPreference()");
        return ret;
    } // getAdminPreference

    /**
     * Adds a admin preference to the database. Returns false if admin already exists.
     */
    public boolean addAdminPreference(Admin admin, String certificatefingerprint, AdminPreference adminpreference){
        debug(">addAdminPreference(fingerprint : " + certificatefingerprint + ")");
        boolean ret = false;
        try {
            AdminPreferencesDataLocal apdata= adminpreferenceshome.create(certificatefingerprint, adminpreference);
            getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,"Administrator preference added.");
            ret = true;
        }
        catch (Exception e) {
          ret = false;
          getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,"Trying to add preference for administrator that already exists.");
        }
        debug("<addAdminPreference()");
        return ret;
    } // addAdminPreference

    /**
     * Changes the admin preference in the database. Returns false if admin doesn't exists.
     */
    public boolean changeAdminPreference(Admin admin, String certificatefingerprint, AdminPreference adminpreference){
       debug(">changeAdminPreference(fingerprint : " + certificatefingerprint + ")");
       return updateAdminPreference(admin, certificatefingerprint, adminpreference, true);
    } // changeAdminPreference

    /**
     * Changes the admin preference in the database. Returns false if admin doesn't exists.
     */
    public boolean changeAdminPreferenceNoLog(Admin admin, String certificatefingerprint, AdminPreference adminpreference){
       debug(">changeAdminPreferenceNoLog(fingerprint : " + certificatefingerprint + ")");
       return updateAdminPreference(admin, certificatefingerprint, adminpreference, false);
    } // changeAdminPreference

    /**
     * Checks if a admin preference exists in the database.
     */
    public boolean existsAdminPreference(Admin admin, String certificatefingerprint){
       debug(">existsAdminPreference(fingerprint : " + certificatefingerprint + ")");
       boolean ret = false;
        try {
            AdminPreferencesDataLocal apdata = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
            ret = true;
        } catch (javax.ejb.FinderException fe) {
             ret=false;
        } catch(Exception e){
          throw new EJBException(e);
        }
        debug("<existsAdminPreference()");
        return ret;
    }// existsAdminPreference

    /**
     * Function that returns the default admin preference.
     *
     * @throws EJBException if a communication or other error occurs.
     */
    public AdminPreference getDefaultAdminPreference(Admin admin){
        debug(">getDefaultAdminPreference()");
        AdminPreference ret =null;
        try {
            AdminPreferencesDataLocal apdata = adminpreferenceshome.findByPrimaryKey(DEFAULTUSERPREFERENCE);
            ret = apdata.getAdminPreference();
        } catch (javax.ejb.FinderException fe) {
            try{
               // Create new configuration
              AdminPreferencesDataLocal apdata = adminpreferenceshome.create(DEFAULTUSERPREFERENCE,new AdminPreference());
              ret = apdata.getAdminPreference();
            }catch(Exception e){
              throw new EJBException(e);
            }  
        } catch(Exception e){
          throw new EJBException(e);
        }
        debug("<getDefaultAdminPreference()");
        return ret;
    } // getDefaultPreference()

     /**
     * Function that saves the default admin preference.
     *
     * @throws EJBException if a communication or other error occurs.
     */
    public void saveDefaultAdminPreference(Admin admin, AdminPreference defaultadminpreference){
       debug(">saveDefaultAdminPreference()");
       try {
          AdminPreferencesDataLocal apdata = adminpreferenceshome.findByPrimaryKey(DEFAULTUSERPREFERENCE);
          apdata.setAdminPreference(defaultadminpreference);
          getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,"Default administrator preference changed.");
       } catch (Exception e) {
           getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ADMINISTRATORPREFERENCECHANGED,"Error saving default administrator preference.");
           throw new EJBException(e);
       }
        debug("<saveDefaultAdminPreference()");
    } // saveDefaultAdminPreference

	/**
	  * Adds a profile to the database.
	  *
	  * @param admin administrator performing task
	  * @param profilename readable profile name
	  * @param profile profile to be added
	  *
	  * @return true if added succesfully, false otherwise if profile already exist
	  */
	 public void addEndEntityProfile(Admin admin, String profilename, EndEntityProfile profile) throws EndEntityProfileExistsException {
		 addEndEntityProfile(admin,findFreeEndEntityProfileId(),profilename,profile);
	 } // addEndEntityProfile

	 /**
	  * Adds a profile to the database.
	  *
	  * @param admin administrator performing task
	  * @param profileid internal ID of new profile, use only if you know it's right.
	  * @param profilename readable profile name
	  * @param profile profile to be added
	  *
	  * @return true if added succesfully, false otherwise if profile already exist
	  */
	 public void addEndEntityProfile(Admin admin, int profileid, String profilename, EndEntityProfile profile) throws EndEntityProfileExistsException{
		if(profilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)){
		  getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA,  new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error adding end entity profile "+ profilename);             
		  throw new EndEntityProfileExistsException(); 
		}  
		 if (isFreeEndEntityProfileId(profileid) == false) {
			getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA,  new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error adding end entity profile "+ profilename);             
		   throw new EndEntityProfileExistsException(); 
		 }
		 try {
			 profiledatahome.findByProfileName(profilename);
			getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA,  new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error adding end entity profile "+ profilename);
			 throw new EndEntityProfileExistsException(); 
		 } catch (FinderException e) {
			 try {
				 profiledatahome.create(new Integer(profileid), profilename, profile);				 
				 getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(), null, null,
					 LogEntry.EVENT_INFO_ENDENTITYPROFILE,
					 "End entity profile " + profilename + " added.");
			 } catch (Exception f) {
				 error("Error adding end entity profile: ", e);
			     logsession.log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(), null, null,
						 LogEntry.EVENT_ERROR_ENDENTITYPROFILE,
						 "Error adding end entity profile " + profilename);				 
			 }
		 }		 
	 } // addEndEntityProfile

     /**
     * Adds a end entity profile to a group with the same content as the original profile.
     */
    public void cloneEndEntityProfile(Admin admin, String originalprofilename, String newprofilename) throws EndEntityProfileExistsException{
       EndEntityProfile profile = null;
       
       if(newprofilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)){
         getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA,  new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error adding end entity profile " + newprofilename +  " using profile " + originalprofilename + " as template.");         
         throw new EndEntityProfileExistsException();
       }
       try{
         EndEntityProfileDataLocal pdl = profiledatahome.findByProfileName(originalprofilename);
         profile = (EndEntityProfile) pdl.getProfile().clone();
         try{
           profiledatahome.findByProfileName(newprofilename);
           getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA,  new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error adding end entity profile " + newprofilename +  " using profile " + originalprofilename + " as template.");       
           throw new EndEntityProfileExistsException();   
         }catch(FinderException e){
            profiledatahome.create(new Integer(findFreeEndEntityProfileId()),newprofilename,profile);           
            getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ENDENTITYPROFILE,"New end entity profile " + newprofilename +  " used profile " + originalprofilename + " as template.");
         }
       }catch(Exception e){
         getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA,  new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error adding end entity profile " + newprofilename +  " using profile " + originalprofilename + " as template.");       
       }

    } // cloneEndEntityProfile

     /**
     * Removes an end entity profile from the database.
     * @throws EJBException if a communication or other error occurs.
     */
    public void removeEndEntityProfile(Admin admin, String profilename) {
      try{
        EndEntityProfileDataLocal pdl = profiledatahome.findByProfileName(profilename);
        pdl.remove();
        getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ENDENTITYPROFILE,"End entity profile " + profilename + " removed.");
      }catch(Exception e){
         getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error removing end entity profile " + profilename + ".");
      }
    } // removeEndEntityProfile

     /**
     * Renames a end entity profile
     */
    public void renameEndEntityProfile(Admin admin, String oldprofilename, String newprofilename) throws EndEntityProfileExistsException{
       if(newprofilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME) || oldprofilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)){
         getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE," Error renaming end entity profile " + oldprofilename + " to " + newprofilename +  "." );  
         throw new EndEntityProfileExistsException();     
       } 
       try{
          profiledatahome.findByProfileName(newprofilename);
          getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE," Error renaming end entity profile " + oldprofilename + " to " + newprofilename +  "." );
          throw new EndEntityProfileExistsException();
       }catch(FinderException e){
         try{
           EndEntityProfileDataLocal pdl = profiledatahome.findByProfileName(oldprofilename);
           pdl.setProfileName(newprofilename);
           getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ENDENTITYPROFILE,"End entity profile " + oldprofilename + " renamed to " + newprofilename +  "." );
         }catch(FinderException f){
           getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE," Error renaming end entity profile " + oldprofilename + " to " + newprofilename +  "." );         
         }
       }
    } // renameProfile

    /**
     * Updates profile data
     */
    public void changeEndEntityProfile(Admin admin, String profilename, EndEntityProfile profile){
       try{
         EndEntityProfileDataLocal pdl = profiledatahome.findByProfileName(profilename);
         pdl.setProfile(profile);
         getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ENDENTITYPROFILE,"End entity profile " +  profilename + " edited.");
       }catch(FinderException e){
         getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ENDENTITYPROFILE,"Error editing end entity profile " + profilename + ".");       
       }
    }// changeEndEntityProfile

    /**
     * Retrives a Collection of id:s (Integer) to authorized profiles.
     */
    public Collection getAuthorizedEndEntityProfileIds(Admin admin){
      ArrayList returnval = new ArrayList();
      Collection result = null;
      
      HashSet authorizedcaids = new HashSet(getAuthorizationSession().getAuthorizedCAIds(admin));
      try{
        if(getAuthorizationSession().isAuthorizedNoLog(admin, "/super_administrator"))
          returnval.add(new Integer(SecConst.EMPTY_ENDENTITYPROFILE));
      }catch(AuthorizationDeniedException e){}
          
      try{
        result = profiledatahome.findAll();
        Iterator i = result.iterator();
        while(i.hasNext()){
          EndEntityProfileDataLocal next = (EndEntityProfileDataLocal) i.next();
          // Check if all profiles available CAs exists in authorizedcaids.
          String[] availablecas = next.getProfile().getValue(EndEntityProfile.AVAILCAS, 0).split(EndEntityProfile.SPLITCHAR);
          boolean allexists = true;
          for(int j=0; j < availablecas.length; j++){
            if(!authorizedcaids.contains( new Integer(availablecas[j]))){
              allexists = false;
              break;
            }
          }
          
          if(allexists)
            returnval.add(next.getId());
        }
      }catch(Exception e){}
      return returnval;
    } // getAuthorizedEndEntityProfileNames

    /**
     * Method creating a hashmap mapping profile id (Integer) to profile name (String).
     */    
    public HashMap getEndEntityProfileIdToNameMap(Admin admin){
      HashMap returnval = new HashMap();
      Collection result = null;
      returnval.put(new Integer(SecConst.EMPTY_ENDENTITYPROFILE),EMPTY_ENDENTITYPROFILENAME);
      try{
        result = profiledatahome.findAll();
        Iterator i = result.iterator();
        while(i.hasNext()){
          EndEntityProfileDataLocal next = (EndEntityProfileDataLocal) i.next();    
          returnval.put(next.getId(),next.getProfileName());
        }
      }catch(Exception e){}
      return returnval;
    } // getEndEntityProfileNames

     /**
     * Finds a end entity profile by id.
     */
    public EndEntityProfile getEndEntityProfile(Admin admin, int id){
       EndEntityProfile returnval=null;
       try{
         if(id==SecConst.EMPTY_ENDENTITYPROFILE) 
           returnval = new EndEntityProfile(true);
         if(id!=0 && id != SecConst.EMPTY_ENDENTITYPROFILE)
           returnval = (profiledatahome.findByPrimaryKey(new Integer(id))).getProfile();
       }catch(FinderException e){
         throw new EJBException(e);
       }
       return returnval;
    } // getEndEntityProfile

     /**
     * Finds a end entity profile by id.
     */
    public EndEntityProfile getEndEntityProfile(Admin admin, String profilename){
       EndEntityProfile returnval=null;
       try{
         if(profilename.equals(EMPTY_ENDENTITYPROFILENAME)) 
           returnval = new EndEntityProfile(true);
         else
           returnval = (profiledatahome.findByProfileName(profilename)).getProfile();
       }catch(FinderException e){
         throw new EJBException(e);
       }
       return returnval;
    } // getEndEntityProfile    

     /**
     * Returns a end entity profiles id, given it's profilename
     *
     * @return the id or 0 if profile cannot be found.
     */
    public int getEndEntityProfileId(Admin admin, String profilename){
      int returnval = 0;
      if(profilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME))
        return SecConst.EMPTY_ENDENTITYPROFILE;  
      try{
        Integer id = (profiledatahome.findByProfileName(profilename)).getId();
        returnval = id.intValue();
      }catch(FinderException e){}

      return returnval;
    } // getEndEntityrofileId

     /**
     * Returns a end entity profiles name given it's id.
     *
     * @return profilename or null if profile id doesn't exists.
     */
    public String getEndEntityProfileName(Admin admin, int id){
      String returnval = null;
      if(id == SecConst.EMPTY_ENDENTITYPROFILE)
        return EMPTY_ENDENTITYPROFILENAME;  
      try{
        returnval = (profiledatahome.findByPrimaryKey(new Integer(id))).getProfileName();
      }catch(FinderException e){}

      return returnval;
    } // getEndEntityProfileName



     /**
     * Method to check if a certificateprofile exists in any of the end entity profiles. Used to avoid desyncronization of certificate profile data.
     *
     * @param certificateprofileid the certificatetype id to search for.
     * @return true if certificateprofile exists in any of the end entity profiles.
     */
    public boolean existsCertificateProfileInEndEntityProfiles(Admin admin, int certificateprofileid){
      String[] availablecertprofiles=null;
      boolean exists = false;
      try{
        Collection result = profiledatahome.findAll();
        Iterator i = result.iterator();
        while(i.hasNext() && !exists){
          availablecertprofiles = ((EndEntityProfileDataLocal) i.next()).getProfile().getValue(EndEntityProfile.AVAILCERTPROFILES, 0).split(EndEntityProfile.SPLITCHAR);
          for(int j=0; j < availablecertprofiles.length; j++){
            if(Integer.parseInt(availablecertprofiles[j]) == certificateprofileid){
              exists=true;
              break;
            };
          }
        }
      }catch(Exception e){}

      return exists;
    }
    
     /**
     * Method to check if a CA exists in any of the end entity profiles. Used to avoid desyncronization of CA data.
     *
     * @param caid the caid to search for.
     * @return true if ca exists in any of the end entity profiles.
     */
    public boolean existsCAInEndEntityProfiles(Admin admin, int caid){
      String[] availablecas=null;
      boolean exists = false;
      try{
        Collection result = profiledatahome.findAll();
        Iterator i = result.iterator();
        while(i.hasNext() && !exists){
          availablecas = ((EndEntityProfileDataLocal) i.next()).getProfile().getValue(EndEntityProfile.AVAILCAS, 0).split(EndEntityProfile.SPLITCHAR);
          for(int j=0; j < availablecas.length; j++){
            if(Integer.parseInt(availablecas[j]) == caid){
              exists=true;
              break;
            };
          }
        }
      }catch(Exception e){}

      return exists;
    } // existsCAProfileInEndEntityProfiles    
    
         /**
     * Loads the global configuration from the database.
     *
     * @throws EJBException if a communication or other error occurs.
     */
    public GlobalConfiguration loadGlobalConfiguration(Admin admin)  {
        debug(">loadGlobalConfiguration()");
        if(globalconfiguration != null)
          return globalconfiguration ;
        
        GlobalConfiguration ret=null;
        try{
          GlobalConfigurationDataLocal gcdata = globalconfigurationhome.findByPrimaryKey("0");
          if(gcdata!=null){
            ret = gcdata.getGlobalConfiguration();
          }
        }catch (javax.ejb.FinderException fe) {
             // Create new configuration
             ret = new GlobalConfiguration();
        }
        debug("<loadGlobalConfiguration()");
        return ret;
    } //loadGlobalConfiguration

    /**
     * Saves global configuration to the database.
     *
     * @throws EJBException if a communication or other error occurs.
     */

    public void saveGlobalConfiguration(Admin admin, GlobalConfiguration globalconfiguration)  {
        debug(">saveGlobalConfiguration()");
        String pk = "0";
        try {
          GlobalConfigurationDataLocal gcdata = globalconfigurationhome.findByPrimaryKey(pk);
          gcdata.setGlobalConfiguration(globalconfiguration);
          getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_EDITSYSTEMCONFIGURATION,"");
        }catch (javax.ejb.FinderException fe) {
           // Global configuration doesn't yet exists.
           try{
             GlobalConfigurationDataLocal data1= globalconfigurationhome.create(pk,globalconfiguration);           
             getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_EDITSYSTEMCONFIGURATION,"");
           } catch(CreateException e){
             getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_EDITSYSTEMCONFIGURATION,"");
           }
        }
        this.globalconfiguration=globalconfiguration;
        debug("<saveGlobalConfiguration()");
     } // saveGlobalConfiguration
    

    // Private methods

    private int findFreeEndEntityProfileId(){
      int id = (new Random((new Date()).getTime())).nextInt();
      boolean foundfree = false;

      while(!foundfree){
        try{
          if(id > 1)
            profiledatahome.findByPrimaryKey(new Integer(id));
          id++;
        }catch(FinderException e){
           foundfree = true;
        }
      }
      return id;
    } // findFreeEndEntityProfileId

	private boolean isFreeEndEntityProfileId(int id) {
			boolean foundfree = false;
			try {
				if (id > 1) {
					profiledatahome.findByPrimaryKey(new Integer(id));
				}
			} catch (FinderException e) {
				foundfree = true;
			}
			return foundfree;
		} // isFreeEndEntityProfileId

    /**
     * Changes the admin preference in the database. Returns false if admin doesn't exist.
     */
    private boolean updateAdminPreference(Admin admin, String certificatefingerprint, AdminPreference adminpreference, boolean dolog){
       debug(">updateAdminPreference(fingerprint : " + certificatefingerprint + ")");
       boolean ret = false;
        try {
            AdminPreferencesDataLocal apdata = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
            adminpreferenceshome.remove(certificatefingerprint);
            try{
                AdminPreferencesDataLocal apdata2 = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
            }  catch (javax.ejb.FinderException fe) {
            }
            apdata= adminpreferenceshome.create(certificatefingerprint,adminpreference);
            try{
                AdminPreferencesDataLocal apdata3 = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
            }  catch (javax.ejb.FinderException fe) {
            }
            if (dolog)
              getLogSession().log(admin, admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,"Administrator preference changed.");
            ret = true;
        } catch (javax.ejb.FinderException fe) {
             ret=false;
             if (dolog){
               getLogSession().log(admin,admin.getCAId(), LogEntry.MODULE_RA, new java.util.Date(),null, null, LogEntry.EVENT_ERROR_ADMINISTRATORPREFERENCECHANGED,"Administrator cannot be found i database.");
             }
        } catch(Exception e){
          throw new EJBException(e);
        }
        debug("<updateAdminPreference()");
        return ret;
    } // changeAdminPreference
   

} // LocalRaAdminSessionBean

