package com.jsh.erp.utils;


/**
 * 文件工具类
 * 提供文件操作的工具方法（文件名处理、路径拼接、文件大小格式化等）
 *
 * @author jishenghua
 */
import java.io.*;
import java.util.*;

/**
 *
 * 鏂囦欢澶勭悊宸ュ叿绫? *
 */
public class FileUtils {

	/**
	 * 鍔熴€€鑳? 鍒涘缓鏂囦欢澶?	 *
	 * @param path
	 *            鍙傘€€鏁?瑕佸垱寤虹殑鏂囦欢澶瑰悕绉?	 * @return 杩斿洖鍊? 濡傛灉鎴愬姛true;鍚﹀垯false 濡傦細FileUtils.mkdir("/usr/apps/upload/");
	 */
	public static boolean makedir(String path) {
		File file = new File(path);
		if (!file.exists())
			return file.mkdirs();
		else
			return true;
	}

	/**
	 * 淇濆瓨鏂囦欢
	 *
	 * @param stream
	 * @param path
	 *            瀛樻斁璺緞
	 * @param filename
	 *            鏂囦欢鍚?	 * @throws IOException
	 */
	public static void SaveFileFromInputStream(InputStream stream, String path, String filename)
			throws IOException {
		File file = new File(path);
		boolean flag=true;
		if(!file.exists()){
			flag=file.mkdirs();
		}
		if(flag){
			FileOutputStream fs = new FileOutputStream(new File(path+filename));
			byte[] buffer = new byte[1024 * 1024];
			int byteread = 0;
			while ((byteread = stream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
				fs.flush();
			}
			fs.close();
			stream.close();
		}
	}


	/**
	 * 鍒楀嚭鏌愪釜鐩綍涓嬬殑鎵€鏈夋枃浠?瀛愮洰褰曚笉鍒楀嚭
	 * @param folderPath:鏂囦欢澶硅矾寰?	 * @return
	 */
	public static List<String> listFile(String folderPath){
		List<String> fileList = new ArrayList<String>(); //FileViewer.getListFiles(destPath, null, false);
		File f = new File(folderPath);
		File[] t = f.listFiles();
		for(int i = 0; i < t.length; i++){
			fileList.add(t[i].getAbsolutePath());
		}
		return fileList;
	}


	/**
	 * 鍒ゆ柇鏂囦欢鏄惁瀛樺湪
	 *
	 * @param fileName
	 * @return
	 */
	public static boolean exists(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 鑾峰彇鏂囦欢鎵╁睍鍚?	 *
	 * @param fileName
	 * @return
	 * */
	public static String getFileExtendName(String fileName) {
		if (fileName == null) {
			return "";
		} else {
			return fileName.substring(fileName.lastIndexOf(".") + 1, fileName
					.length());
		}
	}

	/**
	 * 鍒涘缓涓€涓柊鏂囦欢锛屽鏋滃瓨鍦ㄥ垯鎶ラ敊
	 *
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	public static void createFile(String filePath, String fileName)
			throws RuntimeException {
		String file = null;
		if (filePath == null) {
			file = fileName;
		} else {
			file = filePath + File.separator + fileName;
		}
		createFile(file);
	}

	/**
	 * 鍒涘缓涓€涓柊鏂囦欢(鍚矾寰?锛屽鏋滃瓨鍦ㄥ垯鎶ラ敊
	 *
	 * @param fileName
	 *            鍚湁璺緞鐨勬枃浠跺悕
	 * @return
	 */
	public static void createFile(String fileName) throws RuntimeException {
		File f = new File(fileName);
		if (f.exists()) {
			throw new RuntimeException("FILE_EXIST_ERROR");
		} else {
			try {
				File fileFolder = f.getParentFile();
				if (!fileFolder.exists())
					fileFolder.mkdirs();
				f.createNewFile();
			} catch (IOException ie) {
				System.out.println("鏂囦欢" + fileName + "鍒涘缓澶辫触锛? + ie.getMessage());
				throw new RuntimeException("FILE_CREATE_ERROR");
			}
		}
	}


	/**
	 * 鍒涘缓鐩綍锛屽鏋滃瓨鍦ㄥ垯涓嶅垱寤?	 *
	 * @param path
	 * @return 杩斿洖缁撴灉null鍒欏垱寤烘垚鍔燂紝鍚﹀垯杩斿洖鐨勬槸閿欒淇℃伅
	 * @return
	 */
	public static String createDir(String path, boolean isCreateSubPah) {
		String msg = null;
		File dir = new File(path);

		if (dir == null) {
			msg = "涓嶈兘鍒涘缓绌虹洰褰?;
			return msg;
		}
		if (dir.isFile()) {
			msg = "宸叉湁鍚屽悕鏂囦欢瀛樺湪";
			return msg;
		}
		if (!dir.exists()) {
			if (isCreateSubPah && !dir.mkdirs()) {
				msg = "鐩綍鍒涘缓澶辫触锛屽師鍥犱笉鏄?;
			} else if (!dir.mkdir()) {
				msg = "鐩綍鍒涘缓澶辫触锛屽師鍥犱笉鏄?;
			}
		}
		return msg;
	}

	/**
	 * 鍒犻櫎鎸囧畾鐩綍鎴栨枃浠躲€?濡傛灉瑕佸垹闄ゆ槸鐩綍锛屽悓鏃跺垹闄ゅ瓙鐩綍涓嬫墍鏈夌殑鏂囦欢
	 *
	 * @file:File 鐩綍
	 * */
	public static void delFileOrFolder(String fileName) {
		if (!exists(fileName))
			return;
		File file = new File(fileName);
		delFileOrFolder(file);
	}

	/**
	 * 鍒犻櫎鎸囧畾鐩綍鎴栨枃浠躲€?濡傛灉瑕佸垹闄ゆ槸鐩綍锛屽悓鏃跺垹闄ゅ瓙鐩綍涓嬫墍鏈夌殑鏂囦欢
	 *
	 * @file:File 鐩綍
	 * */
	public static void delFileOrFolder(File file) {
		if (!file.exists())
			return;
		if (file.isFile()) {
			file.delete();
		} else {
			File[] sub = file.listFiles();
			if (sub == null || sub.length <= 0) {
				file.delete();
			} else {
				for (int i = 0; i < sub.length; i++) {
					delFileOrFolder(sub[i]);
				}
				file.delete();
			}
		}
	}

	/**
	 * 浠嶱roperties鏍煎紡閰嶇疆鏂囦欢涓幏鍙栨墍鏈夊弬鏁板苟淇濆瓨鍒癏ashMap涓€?	 * 閰嶇疆涓殑key鍊煎嵆map琛ㄤ腑鐨刱ey鍊硷紝濡傛灉閰嶇疆鏂囦欢淇濆瓨鏃剁敤鐨勪腑鏂囷紝鍒欒繑鍥炵粨鏋滀篃浼氳浆鎴愪腑鏂囥€?	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static HashMap readPropertyFile(String file, String charsetName) throws IOException {
		if (charsetName==null || charsetName.trim().length()==0){
			charsetName="gbk";
		}
		HashMap map = new HashMap();
		InputStream is =null;
		if(file.startsWith("file:"))
			is=new FileInputStream(new File(file.substring(5)));
		else
			is=FileUtils.class.getClassLoader().getResourceAsStream(file);
		Properties properties = new Properties();
		properties.load(is);
		Enumeration en = properties.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String code = new String(properties.getProperty(key).getBytes(
					"ISO-8859-1"), charsetName);
			map.put(key, code);
		}
		return map;
	}
	/**
	 *
	 * @param path
	 *            鏂囦欢璺緞
	 * @param suffix
	 *            鍚庣紑鍚?	 * @param isdepth
	 *            鏄惁閬嶅巻瀛愮洰褰?	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List getListFiles(String path, String suffix, boolean isdepth) {
		File file = new File(path);
		return FileUtils.listFile(file, suffix, isdepth);
	}

	/**
	 * @param f
	 * @param suffix锛氬悗缂€鍚?	 * @param isdepth锛氭槸鍚﹂亶鍘嗗瓙鐩綍
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List listFile(File f, String suffix, boolean isdepth) {
		// 鏄洰褰曪紝鍚屾椂闇€瑕侀亶鍘嗗瓙鐩綍
		List<String> fileList = new ArrayList<String>();
		if (f.isDirectory() && isdepth == true) {
			File[] t = f.listFiles();
			for (int i = 0; i < t.length; i++) {
				listFile(t[i], suffix, isdepth);
			}
		} else {
			String filePath = f.getAbsolutePath();

			if (suffix != null) {
				int begIndex = filePath.lastIndexOf(".");// 鏈€鍚庝竴涓?(鍗冲悗缂€鍚嶅墠闈㈢殑.)鐨勭储寮?				String tempsuffix = "";

				if (begIndex != -1)// 闃叉鏄枃浠朵絾鍗存病鏈夊悗缂€鍚嶇粨鏉熺殑鏂囦欢
				{
					tempsuffix = filePath.substring(begIndex + 1, filePath
							.length());
				}

				if (tempsuffix.equals(suffix)) {
					fileList.add(filePath);
				}
			} else {
				// 鍚庣紑鍚嶄负null鍒欎负鎵€鏈夋枃浠?				fileList.add(filePath);
			}

		}

		return fileList;
	}

	/**
	 * 鍒ゆ柇鏂囦欢鍚嶆槸鍚﹀甫鐩樼锛岄噸鏂板鐞?	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName){
		//鍒ゆ柇鏄惁甯︽湁鐩樼淇℃伅
		// Check for Unix-style path
		int unixSep = fileName.lastIndexOf('/');
		// Check for Windows-style path
		int winSep = fileName.lastIndexOf('\\');
		// Cut off at latest possible point
		int pos = (winSep > unixSep ? winSep : unixSep);
		if (pos != -1)  {
			// Any sort of path separator found...
			fileName = fileName.substring(pos + 1);
		}
		//鏇挎崲涓婁紶鏂囦欢鍚嶅瓧鐨勭壒娈婂瓧绗?		fileName = fileName.replace("=","").replace(",","").replace("&","");
		return fileName;
	}
}
