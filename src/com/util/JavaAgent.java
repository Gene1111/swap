package com.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.bojoy.agent.JavaDynAgent;
import com.google.common.base.Preconditions;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class JavaAgent {

	private VirtualMachine vm;
	private AtomicBoolean running = new AtomicBoolean(false);
	
	private static String AGENT_JAR_PATH = "lib/javaagent.jar";
	private static JavaAgent instance = new JavaAgent();
	
	public static JavaAgent getInst() {
		return instance;
	}

	private void init(String pid)
			throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
		vm = VirtualMachine.attach(pid);
		vm.loadAgent(AGENT_JAR_PATH);

		Instrumentation instrumentation = JavaDynAgent.getInstrumentation();
		Preconditions.checkNotNull(instrumentation, "initInstrumentation must not be null");
	}

	public void swap(String pid, List<SwapJarConfig> configs) {
		if(configs == null || configs.isEmpty()) {
			return;
		}
		if(running.get()) {
			System.out.println("swap is running");
			return;
		}
		running.set(true);
		try {
			redefinedJar(pid, configs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			running.set(false);
		}
	}
	
	private void destroy() throws IOException {
		if (vm != null) {
			vm.detach();
		}
	}

	private boolean redefinedJar(String pid, List<SwapJarConfig> configs) throws ClassNotFoundException, IOException,
			UnmodifiableClassException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
		List<ClassConfig> ret = getClassFileToByte(configs);
		if(ret == null || ret.isEmpty()) {
			return false;
		}
		boolean result = true;
		init(pid);
		try {
			List<ClassDefinition> classDefList = new ArrayList<ClassDefinition>();
			for(ClassConfig config: ret) {
				// 1.整理需要重定义的类
//				Class<?> c = getClass(config);
				Class<?> c = Class.forName(config.getClassName());
				ClassDefinition classDefinition = new ClassDefinition(c, config.getBytes());
				classDefList.add(classDefinition);
			}
			// 2.redefine
			JavaDynAgent.getInstrumentation().redefineClasses(classDefList.toArray(new ClassDefinition[classDefList.size()]));
		} finally {
			destroy();
		}
		return result;
	}

	public Class<?> getClasssFromJarFile(String jarPaht, String filePaht) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarPaht);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();

		Enumeration<JarEntry> ee = jarFile.entries();
		while (ee.hasMoreElements()) {
			JarEntry entry = (JarEntry) ee.nextElement();
			// 过滤我们出满足我们需求的东西
			if (entry.getName().startsWith(filePaht.replace(".", "/")) && entry.getName().endsWith(".class")) {
				jarEntryList.add(entry);
			}
		}
		for (JarEntry entry : jarEntryList) {
			String className = entry.getName().replace('/', '.');
			className = className.substring(0, className.length() - 6);

			try {
				return Thread.currentThread().getContextClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private Class<?> getClass(ClassConfig config) {
		Class<?> c = null; 
		try {
			c = Class.forName(config.getClassName());
		} catch (ClassNotFoundException e) {
			c = getClasssFromJarFile(config.getJarPath(), config.getClassName());
		}
		return c;
	}
	
	@SuppressWarnings("resource")
	private List<ClassConfig> getClassFileToByte(List<SwapJarConfig> configs) {
		List<ClassConfig> list = new ArrayList<ClassConfig>();
		for(SwapJarConfig config : configs) {
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(config.getJarPath());
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
			Enumeration<JarEntry> ee = jarFile.entries();
			List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
			boolean addPath = config.getClzss() == null ? false : true;
			ClassConfig classConfig = new ClassConfig(config.getJarPath());
			while (ee.hasMoreElements()) {
				JarEntry entry = (JarEntry) ee.nextElement();
				if(entry.getName().endsWith(".class")) {
					int size = entry.getName().length();
					String className = entry.getName().substring(0, size - 6).replaceAll("/", ".");
					if(addPath) {
						if (config.getClzss().contains(className)) {
							jarEntryList.add(entry);
							classConfig.setClassName(className);
							System.out.println("reload class name:" +className);
						}
					}else {
						jarEntryList.add(entry);
						classConfig.setClassName(className);
						System.out.println("reload class name:" +className);
					}
				}
			}
			InputStream input = null;
			ByteArrayOutputStream output = null;
			try {
				input = jarFile.getInputStream(jarEntryList.get(0));
				output = new ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}
				byte[] ret = output.toByteArray();
				classConfig.setBytes(ret);
				list.add(classConfig);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				if(input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return list;
	}
}