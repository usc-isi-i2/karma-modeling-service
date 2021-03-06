/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.modeling.rep;

import edu.isi.modeling.common.Namespaces;
import edu.isi.modeling.common.Prefixes;
import edu.isi.modeling.common.Uris;


public class DataPropertyOfColumnLink extends LabeledLink {

	private final String specializedColumnHNodeId;
	private final String specializedLinkId;
	private static final long serialVersionUID = 1L;
	private static final Label label = 
			new Label(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV);

	public DataPropertyOfColumnLink(String id, String hNodeId, String specializedLinkId) {
		super(id, label, LinkType.DataPropertyOfColumnLink);
		this.specializedColumnHNodeId = hNodeId;
		this.specializedLinkId = specializedLinkId;
	}

	public static Label getFixedLabel() {
		return label;
	}

	public String getSpecializedLinkId() {
		return specializedLinkId;
	}
	
	public String getSpecializedColumnHNodeId() {
		return specializedColumnHNodeId;
	}
}
