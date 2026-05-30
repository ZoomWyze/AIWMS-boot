package com.jsh.erp.service;


/**
 * 功能开关 Service
 * 提供系统功能模块的启用/禁用开关管理
 *
 * @author jishenghua
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 鍔熻兘寮€鍏虫湇鍔°€?
 */
@Service
public class FeatureSwitchService {

	/**
	 * 姣曡妯″紡寮€鍏筹細true 琛ㄧず姣曡妯″紡锛堝叧闂晢涓氬寲鍏ュ彛锛夛紝false 琛ㄧず鍟嗕笟妯″紡銆?
	 */
	@Value("${biz.graduationMode:true}")
	private Boolean graduationMode;

	public boolean isGraduationMode() {
		return Boolean.TRUE.equals(graduationMode);
	}

	/**
	 * 鍟嗕笟鍖栧叆鍙ｆ槸鍚﹀彲鐢ㄣ€?
	 */
	public boolean isCommercialEntryEnabled() {
		return !isGraduationMode();
	}
}

