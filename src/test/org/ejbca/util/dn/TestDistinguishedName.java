package org.ejbca.util.dn;

import javax.naming.ldap.Rdn;
import junit.framework.TestCase;

/** Tests for DistinguishedName class.
 * 
 * @author David Galichet
 * @version $Id$
 */
public class TestDistinguishedName extends TestCase {
    
    private static final String DN = "cn=David Galichet,o=Fimasys,email=dgalichet@fimasys.fr," 
        + "g=M,email=david.galichet@fimasys.fr";
    private static final String OTHER_DN = "o=Linagora,email=dgalichet@linagora.fr,ou=Linagora Secu," 
        + "l=Paris,email=david.galichet@linagora.com,email=dgalichet@linagora.com";
    DistinguishedName dn = null;
    DistinguishedName otherDn = null;
  
    private static final String SUBJECT_ALT_NAME = "RFC822NAME=vkn@linagora.com,IPADDRESS=208.77.188.166";
    private static final String OTHER_SUBJECT_ALT_NAME = "RFC822NAME=linagora.mail@linagora.com,IPADDRESS=777.77.777.777,UNIFORMRESOURCEID=other.uri";
    DistinguishedName subjectAltName = null;
    DistinguishedName otherSubjectAltName = null;
    public TestDistinguishedName(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        otherDn = new DistinguishedName(OTHER_DN);
        otherSubjectAltName = new DistinguishedName(OTHER_SUBJECT_ALT_NAME);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetRdn() throws Exception {
        dn = createNewDN();
        assertEquals(dn.getRdn("cn"), new Rdn("cn", "David Galichet"));
        assertEquals(dn.getRdn("email", 1), new Rdn("email", "dgalichet@fimasys.fr"));
        assertEquals(dn.getRdn("email", 0), new Rdn("email", "david.galichet@fimasys.fr"));
        assertNull(dn.getRdn("email", 2));
    }

    /**
     * Test of mergeDN method, of class DistinguishedName.
     * This version tests the merge without override.
     */
    public void testMergeDnWithoutOverride() throws Exception {

        final String EXPECTED_DN = "cn=David Galichet,o=Fimasys,email=dgalichet@fimasys.fr,"
            + "g=M,email=david.galichet@fimasys.fr,ou=Linagora Secu,email=dgalichet@linagora.com,l=Paris";
        dn = createNewDN();
        DistinguishedName newDn = dn.mergeDN(otherDn, false, false, "");

        assertEquals(EXPECTED_DN, newDn.toString());
    }

    /**
     * Test of mergeDN method, of class DistinguishedName.
     * This version tests the merge with override.
     */
    public void testMergeDnWithOverride() throws Exception {

        final String EXPECTED_DN = "cn=David Galichet,o=Linagora,email=dgalichet@linagora.fr,"
            + "g=M,email=david.galichet@linagora.com,ou=Linagora Secu,email=dgalichet@linagora.com,l=Paris";

        dn = createNewDN();
        DistinguishedName newDn = dn.mergeDN(otherDn, true, false, "");

        assertEquals(EXPECTED_DN, newDn.toString());
    }

    /**
     * Test of mergeDN method, of class DistinguishedName.
     * This version tests the merge without override.
     */
    public void testMergeSubjectAltNameWithoutOverrideNotUsingEntityEmail() throws Exception {

        final String EXPECTED = "RFC822NAME=vkn@linagora.com,IPADDRESS=208.77.188.166,UNIFORMRESOURCEID=other.uri";
        subjectAltName = createNewSubjectAltName();
        DistinguishedName altName = subjectAltName.mergeDN(otherSubjectAltName, false, false, "entitymail@linagora.com");

        assertEquals(3, altName.size());

        assertEquals(EXPECTED, altName.toString());
    }

    /**
     * Test of mergeDN method, of class DistinguishedName.
     * This version tests the merge without override.
     */
    public void testMergeSubjectAltNameWithoutOverrideUsingEntityEmail() throws Exception {

    	final String EXPECTED = "RFC822NAME=vkn@linagora.com,IPADDRESS=208.77.188.166,UNIFORMRESOURCEID=other.uri";
        subjectAltName = createNewSubjectAltName();
        DistinguishedName altName = subjectAltName.mergeDN(otherSubjectAltName, false, true, "entitymail@linagora.com");

        assertEquals(EXPECTED, altName.toString());
    }
    /**
     * Test of mergeDN method, of class DistinguishedName.
     * This version tests the merge with override.
     */
    public void testMergeSubjectAltNameWithOverrideNotUsingEntityEmail() throws Exception {

    	final String EXPECTED = "RFC822NAME=linagora.mail@linagora.com,IPADDRESS=777.77.777.777,UNIFORMRESOURCEID=other.uri";
        subjectAltName = createNewSubjectAltName();
        DistinguishedName altName = subjectAltName.mergeDN(otherSubjectAltName, true, false, "entitymail@linagora.com");

        assertEquals(EXPECTED, altName.toString());
    }
    /**
     * Test of mergeDN method, of class DistinguishedName.
     * This version tests the merge with override.
     */
    public void testMergeSubjectAltNameWithOverrideUsingEntityEmail() throws Exception {

    	final String EXPECTED = "RFC822NAME=entitymail@linagora.com,IPADDRESS=777.77.777.777,UNIFORMRESOURCEID=other.uri";
        subjectAltName = createNewSubjectAltName();
        DistinguishedName altName = subjectAltName.mergeDN(otherSubjectAltName, true, true, "entitymail@linagora.com");

        assertEquals(EXPECTED, altName.toString());
    }

    private DistinguishedName createNewDN() throws Exception {
        return new DistinguishedName(DN);
    }

    private DistinguishedName createNewSubjectAltName() throws Exception {
        return new DistinguishedName(SUBJECT_ALT_NAME);
    }
}
