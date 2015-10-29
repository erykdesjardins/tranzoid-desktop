package com.ryk.tzandroidutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.os.Environment;

import com.ryk.tzclientlib.TzPreferences;

// File browsing utility
public class TzFileBrowser {
	public TzFileBrowser() {
		
	}
	
	// Returns a JSON string representation of a directory and its content
	public void getFolderContent(String path, PrintWriter w) {
		// Gets all files and directory contained in specified path
		File f = new File(path); 
		File[] files = f.listFiles(); 
		String message = "ok"; 
		
		if (!f.canRead()) message = "auth";
		
		if (files == null) {
			// JSON header, representation of current directory and parent
			w.println("{\"count\":1,\"absolute\":\"" + path + "\",\"message\":\""+message+"\",\"list\":[{\"type\":\"dir\",\"filetype\":\"dir\",\"title\":\"../\"}]}");
			w.flush();
		} else {
			// JSON header, representation of current directory and parent
			w.println("{\"count\":" + String.valueOf(files.length+1) + ",\"absolute\":\"" + path + "\",\"message\":\""+message+"\",\"list\":[{\"type\":\"dir\",\"filetype\":\"dir\",\"title\":\"../\"}");
			w.flush();
			
			// For each files and directories
			for (File inFile : files) {
				// Whether the item is a file or a directory
			    if (inFile.isDirectory()) {
			    	// Directory as no type
			    	w.println(",{\"type\":\"dir\",\"filetype\":\"dir\",\"title\":\"" + inFile.getName() + "\"}");
			    } else {
			    	// File has more properties
			    	int extIndex = inFile.getName().lastIndexOf(".");
			    	w.println(",{\"type\":\"file\",\"filetype\":\"" + (extIndex == -1 ? "file" : inFile.getName().substring(extIndex+1)) + "\",\"title\":\"" + inFile.getName() + "\"}");
			    }
			    
				// Closes JSON and return the whole thing
				
			    w.flush();
			}
			
			w.println("]}");
		}
	
		w.flush();
	}
	
	public Boolean printFile(String path, PrintWriter w) {
		File f = new File(path);
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			
			String line = reader.readLine();
			if (line == null) w.write(" ");
			
			while (line != null) {
				w.write(line + "\n");
				w.flush();
				
				line = reader.readLine();
			}
			
			w.flush();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	// Append (0), (1) or (n) to a file if original filename
	// already exists. Returns file with new path
	public File appendExitingIndex(File file) {
		int index = 1;
		File newFile = new File(file.getAbsolutePath());
		
		while (newFile.exists()) {
			newFile = new File(file.getAbsolutePath() + "(" + index + ")");
			index++;
		}
			
		return newFile;
	}	
	
	// Reads a single line from a file and closes it
	public String readSingleString(File file) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			
			String line = reader.readLine();
			reader.close();
			
			return line;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public File getNewFileAtDefaultDirectory(String file) {
		return new File(Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + file);
	}
	
	// Create a new folder if not already existing
	public Boolean createFolder(String path) {
		File file = new File(path);
		
		if (!file.exists()) {
			file.mkdir();
			return true;
		}
		
		return false;
	}
	
	public String createFile(File file) {
		file = appendExitingIndex(file);
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return file.getName();
	}
	
	public Boolean overwriteFileContent(File file, String content) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(content);
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	// Copy a file from a source to the given destination
	public Boolean copyFile(File source, File destination) {
		if (source.exists()) {
			try {
				java.io.FileInputStream in = new java.io.FileInputStream(source);
				java.io.FileOutputStream out = new java.io.FileOutputStream(destination);
	
			    // Transfer bytes from in to out
			    byte[] buf = new byte[1024];
			    int len;
			    
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    
			    in.close();
			    out.close();		
			} catch (IOException io) {
				return false;
			}
			
			return true;
		}		
		
		return false;
	}
	
	// Move a file from a given source to a given destination
	public Boolean moveFile(File source, File destination) {
		return source.exists() && source.renameTo(destination);
	}
	
	// Return the parent path of the specified directory
	public String getParentPath(String path) {
		return new File(path).getParent();
	}
	
	// Delete a file at a given path
	public Boolean deleteFile(String path, String filename) {
		return deleteFile(path + "/" + filename);
	}
	
	// Delete a file at an absolute path
	public Boolean deleteFile(String path) {
		return new File(path).delete();
	}
	
	// Prints all files from a root folder recursively
	public void listFiles(String path, String exclusion, PrintWriter w) {
		File dir = new File(path);
		exclusion = exclusion == null ? "" : exclusion;
		
		// For each files contained in a folder
		for (File file : dir.listFiles()) {
			// If the item is a directory, call this function again with this directory
			// Or else, append the file and continue through current folder
			if (file.getName().contains(exclusion)) ;
			else if (file.isDirectory()) listFiles(file.getAbsolutePath(), exclusion, w);
			else if (file.isFile()) w.println(file.getAbsolutePath() + ",");
			
			w.flush();
		}
	}
	
	// Returns an array containing all wallpapers uploaded by user
	public String[] getTzWallpapers() {
		File dir = new File(Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.wallpaperDir);
	
		if (dir.isDirectory()) {
			return dir.list();
		} else {
			return new String[0];
		}
	}
	
	public String getTzWallpapersJson() {
		String[] wallpapers = getTzWallpapers();
		String str = "[";
		
		for (int i = 0; i < wallpapers.length; i++) {
			str += "\"" + wallpapers[i] + "\"";
			
			if (i+1 < wallpapers.length) str += ",";
		}
		str += "]";
		
		return str;
	}
	
	// Returns the response content-type according to the file extention 
	public String getContentType(String filename) {
		int i = filename.lastIndexOf('.');
		String extension = "";
		
		if (i > 0) {
		    extension = filename.substring(i+1);
		}
		
		if (extension.equals("js")) return "text/javascript";
		else if (extension.equals("css")) return "text/css";
		else if (extension.equals("png")) return "image/png";
		else if (extension.equals("jpg") || extension.equals("jpeg")) return "image/jpeg";
		else if (extension.equals("mp3") || extension.equals("m4a")) return "audio/mpeg";
		else if (extension.equals("mp4")) return "video/mp4";
		else if (extension.equals("wma")) return "audio/x-ms-wma";
		else if (extension.equals("php") || extension.equals("tzp")) return "text/html";
		else if (extension.equals("eot")) return "application/vnd.ms-fontobject";
		else if (extension.equals("svg")) return "image/svg+xml";
		else if (extension.equals("ttf")) return "application/octet-stream";
		else if (extension.equals("woff")) return "application/font-woff";
		else if (extension.equals("tzsms")) return "application/octet-stream";
		else return "text/plain";
	}
	
	// Lists all files from a root folder recursively
	public void listFiles(String path, List<String> files, String exclusion) {
		File dir = new File(path);
		exclusion = exclusion == null ? "" : exclusion;
		
		// For each files contained in a folder
		for (File file : dir.listFiles()) {
			// If the item is a directory, call this function again with this directory
			// Or else, append the file and continue through current folder
			if (file.getName().contains(exclusion)) ;
			else if (file.isDirectory()) listFiles(file.getAbsolutePath(), files, exclusion);
			else if (file.isFile()) files.add(file.getAbsolutePath());
		}
	}	
	
	  /**
	   * Similar to android.os.Environment.getExternalStorageDirectory(), except that
	   * here, we return all possible storage directories. The Environment class only
	   * returns one storage directory. If you have an extended SD card, it does not
	   * return the directory path. Here we are trying to return all of them.
	   *
	   * SOURCE : http://renzhi.ca/2012/02/03/how-to-list-all-sd-cards-on-android/
	   *
	   * @return
	   */
	  @SuppressWarnings("finally")
	  public static String[] getStorageDirectories() {
			String[] dirs = null;
			BufferedReader bufReader = null;
			try {
				bufReader = new BufferedReader(new FileReader("/proc/mounts"));
				ArrayList<String> list = new ArrayList<String>();
				String line;
				while ((line = bufReader.readLine()) != null) {
					if (line.contains("vfat") || line.contains("/mnt")) {
						StringTokenizer tokens = new StringTokenizer(line, " ");
						String s = tokens.nextToken();
						s = tokens.nextToken(); // Take the second token, i.e. mount point

						if (s.equals(Environment.getExternalStorageDirectory().getPath())) {
							list.add(s);
						}
						else if (line.contains("/dev/block/vold")) {
							if (!line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
								list.add(s);
							}
						}
					}
				}

				dirs = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					dirs[i] = list.get(i);
				}
			}
			catch (FileNotFoundException e) {}
			catch (IOException e) {}
			finally {
				if (bufReader != null) {
					try {
						bufReader.close();
					}
						catch (IOException e) {}
					}

				return dirs;
			}	
		}
}
