package com.cyanbirds.momo.net.request;

import android.content.Context;

import com.cyanbirds.momo.entity.ExpressionGroup;
import com.cyanbirds.momo.listener.DownloadProgressExpressionListener;
import com.cyanbirds.momo.listener.NetFileDownloadListener;
import com.cyanbirds.momo.net.base.ResultPostExecute;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

/**
 * 
 * @Description:表情下载
 * @author zxj
 * @Date:2015年7月22日下午5:37:48
 */
@SuppressWarnings("deprecation")
public class DownloadExpressionRequest extends ResultPostExecute<String> {

	/**
	 * 下载请求
	 * 
	 * @param context
	 * @param url
	 *            请求地址
	 * @param savePath
	 *            保存地址
	 */
	public void request(Context context, String url, String savePath,
			String fileName, final ExpressionGroup expressionGroup) {
		final File file = new File(savePath, fileName);
		FileDownloader.getImpl().create(url)
				.setPath(file.getAbsolutePath())
				.setListener(new NetFileDownloadListener(){
					@Override
					protected void completed(BaseDownloadTask task) {
						onPostExecute(file.getPath());
					}

					@Override
					protected void error(BaseDownloadTask task, Throwable e) {
						onErrorExecute("下载失败");
					}

					@Override
					protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
						int progress = (int) ((totalBytes > 0) ? (soFarBytes * 1.0 / totalBytes) * 100
								: -1);
						if (expressionGroup != null)
							DownloadProgressExpressionListener.getInstance()
									.notifyExpressionProgressChanged(expressionGroup,
											progress, true);
					}
				}).start();
	}

}
