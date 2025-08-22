package org.jamwiki.mail;

import javax.mail.MessagingException;

public interface WikiMail {
    /**
     * @param recipients  The list of recipients as a String[].
     * @param subject     The subject line.
     * @param message     The message itself.
     * @param contentType The type of content. Use one of the constants if you
     *                    want to explicitly use this parameter. The default value is defined in
     *                    the property smtp_default_content_type in the database.
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String[] recipients,
                  String subject,
                  String message,
                  String contentType)
            throws MessagingException;

    /**
     * @param recipients The list of recipients as a String[].
     * @param subject    The subject line.
     * @param message    The message itself.
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String[] recipients,
                  String subject,
                  String message)
            throws MessagingException;

    /**
     * @param recipients The list of recipients as a String[].
     * @param message    The message itself.
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String[] recipients,
                  String message)
            throws MessagingException;

    /**
     * @param toRecipients The list of recipients as a single String. The mail
     *                     addresses must be separated using the separator defined in the property
     *                     smtp_addr_separator in the database.
     * @param subject      The subject line.
     * @param message      The message itself.
     * @param contentType  The type of content. Use one of the constants if you
     *                     want to explicitly use this parameter. The default value is defined in
     *                     the property smtp_default_content_type in the database.
     * @param attachments  A list of names of files that must be attached to the
     *                     mail. You must use the absolute name, i.e. including the directory where
     *                     the file is located
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String toRecipients,
                  String ccRecipients,
                  String bccRecipients,
                  String subject,
                  String message,
                  String contentType,
                  String[] attachments)
            throws MessagingException;

    /**
     * @param recipients  The list of recipients as a single String. The mail
     *                    addresses must be separated using the separator defined in the property
     *                    smtp_addr_separator in the database.
     * @param subject     The subject line.
     * @param message     The message itself.
     * @param contentType The type of content. Use one of the constants if you
     *                    want to explicitly use this parameter. The default value is defined in
     *                    the property smtp_default_content_type in the database.
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String recipients,
                  String subject,
                  String message,
                  String contentType)
            throws MessagingException;

    /**
     * @param recipients The list of recipients as a single String. The mail
     *                   addresses must be separated using the separator defined in the property
     *                   smtp_addr_separator in the database.
     * @param subject    The subject line.
     * @param message    The message itself.
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String recipients,
                  String subject,
                  String message)
            throws MessagingException;

    /**
     * @param recipients The list of recipients as a single String. The mail
     *                   addresses must be separated using the separator defined in the property
     *                   smtp_addr_separator in the database.
     * @param message    The message itself.
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String recipients,
                  String message)
            throws MessagingException;

    /**
     * The central method for sending mails. The other methods basically
     * call this one, setting the missing parameters to null.
     *
     * @param toRecipients  The list of recipients as a String[].
     * @param ccRecipients  The list of recipients on CC as a String[].
     * @param bccRecipients The list of recipients on BCC as a String[].
     * @param subject       The subject line.
     * @param message       The message itself.
     * @param contentType   The type of content. Use one of the constants if you
     *                      want to explicitly use this parameter. The default value is defined in
     *                      the property smtp_default_content_type in the database.
     * @param attachments   A list of names of files that must be attached to the
     *                      mail. You must use the absolute name, i.e. including the directory where
     *                      the file is located
     * @throws MessagingException If there is any problem sending the mail.
     */
    void postMail(String[] toRecipients,
                  String[] ccRecipients,
                  String[] bccRecipients,
                  String subject,
                  String message,
                  String contentType,
                  String[] attachments)
            throws MessagingException;

    /**
     * This method is a shortcut to retrieve the address separator delimiter
     * defined in the database.
     *
     * @return The address separator to use in a String containing many mail
     * addresses.
     */
    String getAddressSeparator();
}
