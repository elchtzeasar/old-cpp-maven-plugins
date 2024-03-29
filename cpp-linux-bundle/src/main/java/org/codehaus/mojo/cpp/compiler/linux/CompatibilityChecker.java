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

package org.codehaus.mojo.cpp.compiler.linux;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.bundle.AbstractBundleCompatibilityChecker;
import org.codehaus.mojo.cpp.tools.environment.Environment;
import org.codehaus.mojo.cpp.tools.environment.EnvironmentManager;


public class CompatibilityChecker extends AbstractBundleCompatibilityChecker {

	public CompatibilityChecker(Log log) {
		super(log);
	}

	@Override
	protected boolean hostSupported(Environment host) {
		if( !commandIsAvailableOnPath("gcc") )
			return false;
		
		return true;
	}

	@Override
	protected boolean targetSupported(Environment target) {
		return target.equals(EnvironmentManager.LINUX_32) || target.equals(EnvironmentManager.LINUX_64);
	}
}
