package com.sparkrico.v2ex.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class FileUtil {

	private static final String NODE_CACHE = "/nodes.cache";

	public static String getNodeCacheDir(Context context) {
		return context.getCacheDir().getAbsolutePath() + NODE_CACHE;
	}

	/**
	 * 取NodeCache文件内容
	 * @param context
	 * @return
	 */
	public static String getNodeCache(Context context) {
		String content = "";
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(FileUtil.getNodeCacheDir(context));
			content = FileUtil.readInputStreamToString(fis);
			fis.close();
		} catch (FileNotFoundException e1) {
			// java.io.FileNotFoundException:
			// /data/data/com.sparkrico.v2ex/cache/nodes.cache (No such file or
			// directory)
			// e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}
		return content;
	}

	/**
	 * 存NodeCache文件
	 * @param context
	 * @param content
	 */
	public static void putNodeCache(Context context, String content) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(FileUtil.getNodeCacheDir(context));
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static String readInputStreamToString(InputStream inputStream) {
		BufferedReader r = new BufferedReader(
				new InputStreamReader(inputStream));
		StringBuilder total = new StringBuilder();
		String line;
		try {
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return total.toString();
	}
}
