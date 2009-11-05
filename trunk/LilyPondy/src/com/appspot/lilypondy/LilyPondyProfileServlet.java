package com.appspot.lilypondy;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class LilyPondyProfileServlet extends ProfileServlet {

	public static final String URL = "http://lilypondy.appspot.com";

	@Override
	public String getRobotName() {
		return "LilyPondy";
	}

	@Override
	public String getRobotProfilePageUrl() {
		return URL;
	}

	@Override
	public String getRobotAvatarUrl() {
		return getRobotProfilePageUrl() + "/images/avatar.png";
	}

}
