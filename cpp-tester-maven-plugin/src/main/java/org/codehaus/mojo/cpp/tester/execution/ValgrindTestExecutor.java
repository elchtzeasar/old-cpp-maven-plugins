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

package org.codehaus.mojo.cpp.tester.execution;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.tester.TestSettings;


public class ValgrindTestExecutor extends AbstractTestExecutor {

	private String suppressionsArgument;

	public ValgrindTestExecutor(final Log log, final TestSettings settings) {
		super(log);
		
		if( settings.getSuppressionFile().exists() )
			suppressionsArgument = "--suppressions=" + settings.getSuppressionFile();
		else
			log.debug("Suppressions file " + settings.getSuppressionFile() + " doesn't exist. Skipping Valgrind suppressions.");
	}
	
	@Override
	public int execute(File testBinary) throws MojoFailureException, MojoExecutionException {
		cli.initialize(testBinary.getParentFile(), "valgrind");
		cli.getCommandline().createArg().setValue(suppressionsArgument);
		
		cli.getCommandline().createArg().setValue("--leak-check=full");
		cli.getCommandline().createArg().setValue("./" + testBinary.getName());
		
		return cli.execute(false);
	}

}
