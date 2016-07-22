package flinkcep.examples.sources;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import flinkcep.examples.common.AlarmEvent;
import flinkcep.examples.common.Severity;

import java.util.Random;

/**
 * Source to generate events. 
 * simulating events from a network elements with random
 * severity. 
 * 
 *
 */
public class AlarmEventSource extends RichParallelSourceFunction<AlarmEvent> {
	private static final long serialVersionUID = 3589767994783688247L;

	private boolean running = true;

	private final long pause;
	private final double temperatureStd;
	private final double temperatureMean;
	private Random random;

	public AlarmEventSource(long pause, double temperatureStd, double temperatureMean) {
		this.pause = pause;
		this.temperatureMean = temperatureMean;
		this.temperatureStd = temperatureStd;
	}

	@Override
	public void open(Configuration configuration) {
		random = new Random();
	}

	public void run(SourceContext<AlarmEvent> sourceContext) throws Exception {
		while (running) {
			AlarmEvent event = null;
			Severity alarmSeverity = null;
			double temperature = random.nextGaussian() * temperatureStd + temperatureMean;
			if (temperature > temperatureMean) {
				alarmSeverity = Severity.CRITICAL;
			}else if (temperature > (temperatureMean-10) && temperature < temperatureMean) {
				alarmSeverity = Severity.MAJOR;
			}else if (temperature > (temperatureMean-20) && temperature < temperatureMean-10) {
				alarmSeverity = Severity.MINOR;
			}else if (temperature > (temperatureMean-30) && temperature < temperatureMean-20) {
				alarmSeverity = Severity.WARNING;
			}else if (temperature < (temperatureMean-30))  {
				alarmSeverity = Severity.CLEAR;
			}
			event = new AlarmEvent(123, "NE1", alarmSeverity);
			event.setSpecificProblem("Temperature Reached : "+ temperature);
			sourceContext.collect(event);
			Thread.sleep(pause);
		}
	}

	public void cancel() {
		running = false;
	}

}
