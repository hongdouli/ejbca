/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.webtest.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * End Entity Profile helper class for EJBCA Web Tests.
 * 
 * @version $Id$
 */
public class EndEntityProfileHelper extends BaseHelper {

    /**
     * Contains constants and references of the 'End Entity Profiles' page.
     */
    private static class Page {
        // General
        static final String PAGE_URI = "/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp";
        static final By PAGE_LINK = By.id("raEditendentityprofiles");
        // End Entity Profiles Form
        static final By TEXT_MESSAGE = By.xpath("//td[contains(text(), 'End Entity Profile saved.')]");
        static final By INPUT_NAME = By.xpath("//input[@name='textfieldprofilename']");
        static final By BUTTON_ADD = By.xpath("//input[@name='buttonaddprofile']");
        static final By BUTTON_EDIT = By.xpath("//input[@name='buttoneditprofile']");
        static final By BUTTON_CLONE = By.xpath("//input[@name='buttoncloneprofile']");
        static final By BUTTON_RENAME = By.xpath("//input[@name='buttonrenameprofile']");
        static final By BUTTON_DELETE = By.xpath("//input[@name='buttondeleteprofile']");
        static final By SELECT_EE_PROFILES = By.xpath("//select[@name='selectprofile']");
        // End Entity Profile Form
        static final By TEXT_TITLE_END_ENTITY_PROFILE = By.xpath("//div/h3");
        static final By INPUT_USERNAME_AUTO_GENERATED = By.id("checkboxautogeneratedusername");
        static final By SELECT_DEFAULT_CERTIFICATE_PROFILE = By.xpath("//select[@name='selectdefaultcertprofile']");
        static final By SELECT_AVAILABLE_CERTIFICATE_PROFILES = By.xpath("//select[@name='selectavailablecertprofiles']");
        static final By SELECT_DEFAULT_CA = By.xpath("//select[@name='selectdefaultca']");
        static final By SELECT_AVAILABLE_CAS = By.xpath("//select[@name='selectavailablecas']");
        static final By BUTTON_SAVE_PROFILE = By.xpath("//input[@name='buttonsave']");
        static final By BUTTON_BACK_TO_END_ENTITY_PROFILES = By.xpath("//td/a[contains(@href,'editendentityprofiles.jsp')]");
        // Dynamic references
        static By getEEPOptionContainingText(final String text) {
            return By.xpath("//option[@value='" + text + "']");
        }

        static By getSubjectAttributesSelectByAttributeType(final String attributeType) {
            return By.xpath("//select[@name='selectadd" + attributeType + "']");
        }

        static By getSubjectAttributesAddButtonByAttributeType(final String attributeType) {
            return By.xpath("//input[@name='buttonadd" + attributeType + "']");
        }

        static By getSubjectAttributesAttributeByAttributeName(final String attributeName) {
            return By.xpath("//td[contains(text(), '" + attributeName + "')]");
        }

        static By getSubjectAttributesAttributeModifiableByAttributeTypeAndAttributeIndex(final String attributeType, final int attributeIndex) {
            return By.id("checkboxmodifyable" + attributeType + attributeIndex);
        }

        static By getSubjectAttributesAttributeTextfieldByAttributeTypeAndAttributeIndex(final String attributeType, final int attributeIndex) {
            return By.xpath("//input[@name='textfield" + attributeType + + attributeIndex + "']");
        }
    }

    public EndEntityProfileHelper(final WebDriver webDriver) {
        super(webDriver);
    }

    /**
     * Opens the 'End Entity Profiles' page and asserts the correctness of URI path.
     *
     * @param webUrl home page URL.
     */
    public void openPage(final String webUrl) {
        openPageByLinkAndAssert(webUrl, Page.PAGE_LINK, Page.PAGE_URI);
    }

    /**
     * Asserts the current URI equals to End Entity Profile page URI.
     */
    public void assertIsOnStartPage() {
        assertPageUri(Page.PAGE_URI);
    }

    /**
     * Adds a new End Entity Profile, and asserts that it appears in End Entities Profiles table.
     *
     * @param endEntityProfileName an End Entity Profile name.
     */
    public void addEndEntityProfile(final String endEntityProfileName) {
        fillInput(Page.INPUT_NAME, endEntityProfileName);
        clickLink(Page.BUTTON_ADD);
        assertEndEntityProfileNameExists(endEntityProfileName);
    }

    /**
     * Opens the edit page for an End Entity Profile, then asserts that the correct End Entity Profile is being edited.
     *
     * @param endEntityProfileName an End Entity Profile name.
     */
    public void openEditEndEntityProfilePage(final String endEntityProfileName) {
        selectOptionByName(Page.SELECT_EE_PROFILES, endEntityProfileName);
        clickLink(Page.BUTTON_EDIT);
        assertEndEntityProfileTitleExists(endEntityProfileName);
    }

    /**
     * Edits the End Entity Profile
     *
     * @param defaultCertificateProfileName the value for 'Default Certificate Profile'.
     * @param selectedCertificateProfiles the list for 'Available Certificate Profiles' selector.
     * @param defaultCAName the value for 'Default CA'.
     * @param selectedCAs the list for 'Available CAs' selector.
     */
    public void editEndEntityProfile(
            final String defaultCertificateProfileName,
            final List<String> selectedCertificateProfiles,
            final String defaultCAName,
            final List<String> selectedCAs
    ) {
        selectOptionByName(Page.SELECT_DEFAULT_CERTIFICATE_PROFILE, defaultCertificateProfileName);
        selectOptionsByName(Page.SELECT_AVAILABLE_CERTIFICATE_PROFILES, selectedCertificateProfiles);
        selectOptionByName(Page.SELECT_DEFAULT_CA, defaultCAName);
        selectOptionsByName(Page.SELECT_AVAILABLE_CAS, selectedCAs);
    }

    /**
     * Saves the End Entity Profile with success assertion.
     */
    public void saveEndEntityProfile() {
        saveEndEntityProfile(true);
    }

    /**
     * Saves the End Entity Profile with assertion.
     *
     * @param withAssertion true for success assertion, false otherwise.
     */
    public void saveEndEntityProfile(final boolean withAssertion) {
        clickLink(Page.BUTTON_SAVE_PROFILE);
        if(withAssertion) {
            assertEndEntityProfileSaved();
        }
    }

    /**
     * Clones the End Entity Profile.
     *
     * @param endEntityProfileName source End Entity Profile name.
     * @param newEndEntityProfileName target End Entity Profile name.
     */
    public void cloneEndEntityProfile(final String endEntityProfileName, final String newEndEntityProfileName) {
        // Select End Entity Profile in list
        selectOptionByName(Page.SELECT_EE_PROFILES, endEntityProfileName);
        // Clone the End Entity Profile
        fillInput(Page.INPUT_NAME, newEndEntityProfileName);
        clickLink(Page.BUTTON_CLONE);
    }

    /**
     * Renames the End Entity Profile.
     *
     * @param oldEndEntityProfileName source End Entity Profile name.
     * @param newEndEntityProfileName target End Entity Profile name.
     */
    public void renameEndEntityProfile(final String oldEndEntityProfileName, final String newEndEntityProfileName) {
        // Select End Entity Profile in list
        selectOptionByName(Page.SELECT_EE_PROFILES, oldEndEntityProfileName);
        // Rename the End Entity Profile
        fillInput(Page.INPUT_NAME, newEndEntityProfileName);
        clickLink(Page.BUTTON_RENAME);
    }

    /**
     * Calls the delete dialog of the End Entity Profile by name.
     *
     * @param endEntityProfileName End Entity Profile name.
     */
    public void deleteEndEntityProfile(final String endEntityProfileName) {
        // Select End Entity Profile in list
        selectOptionByName(Page.SELECT_EE_PROFILES, endEntityProfileName);
        clickLink(Page.BUTTON_DELETE);
    }

    /**
     * Confirms/Discards the deletion of End Entity Profile with expected message.
     *
     * @param message expected message.
     * @param isConfirmed true to confirm deletion, false otherwise.
     */
    public void confirmEndEntityProfileDeletion(final String message, final boolean isConfirmed) {
        assertAndConfirmAlertPopUp(message, isConfirmed);
    }

    /**
     * Asserts the End Entity Profile name exists in the list of End Entity Profiles.
     *
     * @param endEntityProfileName End Entity Profile name.
     */
    public void assertEndEntityProfileNameExists(final String endEntityProfileName) {
        final WebElement selectWebElement = findElement(Page.SELECT_EE_PROFILES);
        if (findElement(selectWebElement, Page.getEEPOptionContainingText(endEntityProfileName)) == null) {
            fail(endEntityProfileName + " was not found in the List of End Entity Profiles.");
        }
    }

    /**
     * Asserts the End Entity Profile name exists in the list of End Entity Profiles.
     *
     * @param endEntityProfileName End Entity Profile name.
     */
    public void assertEndEntityProfileNameDoesNotExist(final String endEntityProfileName) {
        final WebElement selectWebElement = findElement(Page.SELECT_EE_PROFILES);
        if (findElement(selectWebElement, Page.getEEPOptionContainingText(endEntityProfileName)) != null) {
            fail(endEntityProfileName + " was found in the List of End Entity Profiles.");
        }
    }

    /**
     * Triggers the input 'Username' Auto-generated.
     */
    public void triggerUsernameAutoGenerated() {
        clickLink(Page.INPUT_USERNAME_AUTO_GENERATED);
    }

    /**
     * Asserts the element 'Username' Auto-generated is selected/de-selected.
     *
     * @param isSelected true for selected and false for de-selected.
     */
    public void assertUsernameAutoGeneratedIsSelected(final boolean isSelected) {
        assertEquals(
                "'Username' Auto-generated field isSelected [" + isSelected + "]",
                isSelected,
                isSelectedElement(Page.INPUT_USERNAME_AUTO_GENERATED)
        );
    }

    /**
     * Clicks the back link.
     */
    public void triggerBackToEndEntityProfiles() {
        clickLink(Page.BUTTON_BACK_TO_END_ENTITY_PROFILES);
    }

    /**
     * Adds an attribute to 'Subject DN Attributes', 'Subject Alternative Name' or
     * 'Subject Directory Attributes' while editing an End Entity Profile.
     *
     * @param attributeType either 'subjectdn', 'subjectaltname' or 'subjectdirattr'
     * @param attributeName the displayed name of the attribute, e.g. 'O, Organization'
     */
    public void addSubjectAttribute(final String attributeType, final String attributeName) {
        selectOptionByName(Page.getSubjectAttributesSelectByAttributeType(attributeType), attributeName);
        clickLink(Page.getSubjectAttributesAddButtonByAttributeType(attributeType));
    }

    /**
     * Asserts the attribute exists in the section 'Subject DN Attributes'.
     *
     * @param attributeName name of the attribute.
     */
    public void assertSubjectAttributeExists(final String attributeName) {
        final WebElement subjectAttributeWebElement  = findElement(Page.getSubjectAttributesAttributeByAttributeName(attributeName));
        assertNotNull(
                "The attribute " + attributeName + " does not exist",
                subjectAttributeWebElement
        );
    }

    /**
     * Triggers the input 'Modifiable' for an attribute specified by its type and index in the section 'Subject DN Attributes'.
     *
     * @param attributeType attribute's type.
     * @param attributeIndex attribute's index.
     */
    public void triggerSubjectAttributesAttributeModifiable(final String attributeType, final int attributeIndex) {
        clickLink(Page.getSubjectAttributesAttributeModifiableByAttributeTypeAndAttributeIndex(attributeType, attributeIndex));
    }

    /**
     * Asserts the appearance of alert dialog with expected message
     *
     * @param alertMessage expected alert message.
     * @param isConfirmed true to confirm, false otherwise.
     */
    public void assertSubjectAttributesAttributeModifiableAlert(final String alertMessage, final boolean isConfirmed) {
        assertAndConfirmAlertPopUp(alertMessage, isConfirmed);
    }

    /**
     * Sets the value for the attribute specified by its type and index in the section 'Subject DN Attributes'.
     *
     * @param attributeType attribute's type.
     * @param attributeIndex attribute's index.
     * @param value attribute's value.
     */
    public void fillSubjectAttributesAttributeValue(final String attributeType, final int attributeIndex, final String value) {
        fillInput(Page.getSubjectAttributesAttributeTextfieldByAttributeTypeAndAttributeIndex(attributeType, attributeIndex), value);
    }

    // Asserts the title text
    private void assertEndEntityProfileTitleExists(final String endEntityProfileName) {
        final WebElement endEntityProfileTitle = findElement(Page.TEXT_TITLE_END_ENTITY_PROFILE);
        if (endEntityProfileTitle == null) {
            fail("End Entity Profile title was not found.");
        }
        assertEquals(
                "Unexpected title on End Entity Profile 'Edit' page",
                "End Entity Profile : " + endEntityProfileName,
                endEntityProfileTitle.getText()
        );
    }

    // Asserts the success of profile save
    private void assertEndEntityProfileSaved() {
        final WebElement endEntityProfileSaveMessage = findElement(Page.TEXT_MESSAGE);
        if (endEntityProfileSaveMessage == null) {
            fail("End Entity Profile save message was not found.");
        }
        assertEquals(
                "Expected profile save message was not displayed",
                "End Entity Profile saved.",
                endEntityProfileSaveMessage.getText()
        );
    }

    /**
     * Clicks the Cancel button when editing an End Entity Profile.
     *
     * @param webDriver the WebDriver to use
     */
    public static void cancel(WebDriver webDriver) {
        webDriver.findElement(By.xpath("//input[@name='buttoncancel']")).click();
    }

}