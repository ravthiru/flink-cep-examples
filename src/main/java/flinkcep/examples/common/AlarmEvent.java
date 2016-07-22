/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package flinkcep.examples.common;

/**
 * Typical event from Network Element 
 * 
 */
public class AlarmEvent {
	int eventNumber;
	String resourceId;
	Severity severity;
	int probableCause;
	String specificProblem;
	long eventTime;

	public AlarmEvent(int eventNumber, String resourceId, Severity severity) {
		this.eventNumber = eventNumber;
		this.resourceId = resourceId;
		this.severity = severity;
		eventTime = System.currentTimeMillis();
	}

	public int getEventNumber() {
		return eventNumber;
	}

	public void setEventNumber(int eventNumber) {
		this.eventNumber = eventNumber;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public int getProbableCause() {
		return probableCause;
	}

	public void setProbableCause(int probableCause) {
		this.probableCause = probableCause;
	}

	public String getSpecificProblem() {
		return specificProblem;
	}

	public void setSpecificProblem(String specificProblem) {
		this.specificProblem = specificProblem;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	@Override
	public String toString() {
		return "AlarmEvent("+getResourceId()+"," +getEventNumber()+","+getSeverity().getName()+","+ getEventTime()+")";
	}

}
