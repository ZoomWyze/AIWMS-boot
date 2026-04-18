package com.jsh.erp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 功能开关服务。
 */
@Service
public class FeatureSwitchService {

	/**
	 * 毕设模式开关：true 表示毕设模式（关闭商业化入口），false 表示商业模式。
	 */
	@Value("${biz.graduationMode:true}")
	private Boolean graduationMode;

	public boolean isGraduationMode() {
		return Boolean.TRUE.equals(graduationMode);
	}

	/**
	 * 商业化入口是否可用。
	 */
	public boolean isCommercialEntryEnabled() {
		return !isGraduationMode();
	}
}

