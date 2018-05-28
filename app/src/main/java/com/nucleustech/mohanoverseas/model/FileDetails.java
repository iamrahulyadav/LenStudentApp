/**
 * 
 */
package com.nucleustech.mohanoverseas.model;

import java.io.InputStream;

/**
 * @author ritwik.rai
 * 
 */
public class FileDetails {

	private InputStream inputStream;
	private int fileSize;
	private String fileName;
	private String filePath;
	private int actionType;
private String selectedFilePath;
	/**
	 * @return the inputStream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * @param inputStream
	 *            the inputStream to set
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * @return the fileSize
	 */
	public int getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the actionType
	 */
	public int getActionType() {
		return actionType;
	}

	/**
	 * @param actionType
	 *            the actionType to set
	 */
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public String getSelectedFilePath() {
		return selectedFilePath;
	}

	public void setSelectedFilePath(String selectedFilePath) {
		this.selectedFilePath = selectedFilePath;
	}
	
	
}
