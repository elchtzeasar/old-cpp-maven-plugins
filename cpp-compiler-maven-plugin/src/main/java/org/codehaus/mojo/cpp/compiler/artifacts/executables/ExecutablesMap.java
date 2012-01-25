/* 
 *  Copyright 2011 Ericsson AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.codehaus.mojo.cpp.compiler.artifacts.executables;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;
import org.codehaus.mojo.cpp.tools.FileFinder;
import org.codehaus.mojo.cpp.tools.environment.Environment;
import org.codehaus.mojo.cpp.tools.settings.PluginSettingsImpl;


public class ExecutablesMap {
	private final Map<String, Collection<Executable>> map = new HashMap<String, Collection<Executable>>();
	private final Log log;
	private final PluginSettingsImpl settings;
	private final Collection<NativeCodeFile> compiledFiles;

	public ExecutablesMap(final Log log, final Collection<NativeCodeFile> compiledClasses, final PluginSettingsImpl settings) {
		this.log = log;
		this.compiledFiles = compiledClasses;
		this.settings = settings;
	}

	public Collection<Executable> getAllExecutables(final Environment environment) {
		final Collection<Executable> allExecutables = new ArrayList<Executable>();

		if( map.get("all") != null )
			allExecutables.addAll(map.get("all"));

		if( map.get(environment.getCanonicalName()) != null )
			allExecutables.addAll(map.get(environment.getCanonicalName()));

		log.debug("Found " + allExecutables.size() + " defined executables for " + environment.getName());

		return allExecutables;
	}

	public void parseExecutableMapping(final Map<String, String> mapping, final Environment targetEnvironment) throws MojoFailureException {
		for(Map.Entry<String, String> environmentToExecutablesMapping : mapping.entrySet()) {
			final String environmentName = environmentToExecutablesMapping.getKey();
			if( environmentName.equals("all") || environmentName.equals(targetEnvironment.getCanonicalName()) )
				parseExecutableDefinitions(environmentName, environmentToExecutablesMapping.getValue());
		}
	}

	public void addExecutable(final String environment, final String executableName, final String pattern) {
		Collection<NativeCodeFile> files = findFiles(pattern);
		
		if( executableName.equals("[]") )
			for(NativeCodeFile file : files)
				addExecutable(environment, new Executable(file.getClassName(), file));
		else
			addExecutable(environment, new Executable(executableName, files));
	}

	public void addExecutable(final String environment, final Executable executable) {
		if(!map.containsKey(environment))
			map.put(environment, new ArrayList<Executable>());

		map.get(environment).add(executable);
	}

	private void parseExecutableDefinitions(final String environmentName, final String executableDefinitions) throws MojoFailureException {
		log.debug(environmentName + " mapped to " + executableDefinitions);
		for(String executableDefinition : executableDefinitions.split("[;\n\r]+"))
			parseExecutableDefinition(environmentName, executableDefinition);
	}

	private void parseExecutableDefinition(final String environmentName, final String executableDefinition) throws MojoFailureException {
		log.debug("Found definition " + executableDefinition + " for env " + environmentName);
		final String[] splitDefinition = executableDefinition.split("=");

		final String executableName = splitDefinition[0].trim();
		final String executablePattern = splitDefinition.length == 2 ? splitDefinition[1] : null;

		if( splitDefinition.length > 2)
			throw new MojoFailureException("Failed to parse executable definition \"" + executableDefinition + "\". Format must be either \"name\" or \"name=pattern1,pattern2,...\".");

		addExecutable(environmentName, executableName, executablePattern);
	}

	private Collection<NativeCodeFile> findFiles(final String pattern) {
		log.debug("Finding executable files matching pattern " + pattern + ".");
		final Collection<NativeCodeFile> allCppFiles = new ArrayList<NativeCodeFile>();

		if( pattern != null ) {
			for(String patternElement : pattern.split(",")) {
				final Collection<File> matchingFiles = new FileFinder(settings.getProject().getBasedir(), patternElement.trim()).getFiles();
				allCppFiles.addAll(translateRawFilesToCompiledFiles(matchingFiles));
			}

			if( allCppFiles.isEmpty() )
				log.warn("Found no compiled files matching the pattern " + pattern + ".");
		}

		return allCppFiles;
	}

	private Collection<NativeCodeFile> translateRawFilesToCompiledFiles(final Collection<File> rawFiles) {
		final Collection<NativeCodeFile> matchingCompiledFiles = new ArrayList<NativeCodeFile>();

		for(File rawFile : rawFiles) {
			boolean matchFound = false;
			for(NativeCodeFile compiledFile : compiledFiles) {
				if( compiledFile.getSourceFile().equals(rawFile)) {
					matchingCompiledFiles.add(compiledFile);
					matchFound = true;
					break;
				}
			}

			if( !matchFound )
				log.warn("Could not find " + rawFile + " among the list of compiled files.");
		}
		return matchingCompiledFiles;
	}
}
