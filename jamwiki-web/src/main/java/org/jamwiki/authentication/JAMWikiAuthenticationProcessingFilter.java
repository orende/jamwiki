/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.authentication;

import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * This class is a hack implemented to support virtual wikis and Spring Security.
 */
public class JAMWikiAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {

	private static final WikiLogger logger = WikiLogger.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

	/**
	 *
	 */
	public JAMWikiAuthenticationProcessingFilter() {
		super();
		((SimpleUrlAuthenticationSuccessHandler)this.getSuccessHandler()).setTargetUrlParameter(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME);
	}

	/**
	 * Indicates whether this filter should attempt to process a login request
	 * for the current invocation.
	 *
	 * It strips any parameters from the "path" section of the request URL
	 * (such as the jsessionid parameter in
	 * http://host/myapp/index.html;jsessionid=blah) before matching against
	 * the filterProcessesUrl property.
	 *
	 * FIXME - This method is needed due to the fact that different virtual
	 * wikis may be used.
	 */
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI();
        logger.debug("JAMWikiAuthenticationProcessingFilter.requiresAuthentication: " + uri);
		uri = WikiUtil.stripSemicolon(uri);
		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
		return uri.endsWith(request.getContextPath() + "/" + virtualWiki + "/j_spring_security_check");
	}

	/**
	 * Override the parent method to update the last login date on successful
	 * authentication.
	 */
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, auth);
		Object principal = auth.getPrincipal();
		// find authenticated username
		String username = null;
		if (principal instanceof UserDetails userDetails) {
			// using custom authentication with Spring Security UserDetail service
			username = userDetails.getUsername();
		} else if (principal instanceof String principalString) {
			// external authentication returns only username
			username = principalString;
		}
		if (username != null) {
			try {
				WikiUser wikiUser = WikiBase.getDataHandler().lookupWikiUser(username);
				if (wikiUser != null) {
					wikiUser.setLastLoginDate(new Timestamp(System.currentTimeMillis()));
                    WikiGroup registeredUsersGroup = WikiBase.getDataHandler().lookupWikiGroup(WikiGroup.GROUP_REGISTERED_USER);
                    WikiBase.getDataHandler().writeWikiUser(wikiUser, wikiUser.getUsername(), "", registeredUsersGroup);
					// update password reset challenge fields, just in case
					wikiUser.setChallengeValue(null);
					wikiUser.setChallengeDate(null);
					wikiUser.setChallengeIp(null);
					wikiUser.setChallengeTries(0);
					WikiBase.getDataHandler().updatePwResetChallengeData(wikiUser);
				}
			} catch (WikiException e) {
				// log but do not throw - failure to update last login date is non-fatal
				logger.error("Failure while updating last login date for " + username, e);
			}
		}
	}

    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        logger.debug("Setting up AuthenticationManager: %s".formatted(authenticationManager));
        super.setAuthenticationManager(authenticationManager);
    }
}
