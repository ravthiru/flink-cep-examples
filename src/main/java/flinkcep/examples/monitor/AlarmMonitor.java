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

package flinkcep.examples.monitor;

import java.util.Map;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.types.Either;

import flinkcep.examples.common.AlarmEvent;
import flinkcep.examples.common.Severity;
import flinkcep.examples.sources.AlarmEventSource;

/*
 * When network element detects change in temperature it sends a event with changed severity. 
   Monitoring user should not see events where temperature continuously 
   flapping at threshold like Critical-Clear, Critical-Clear etc.
   If there is no clear event with in a time window then the Critical is shown to 
   the monitoring User as Critical Alarm , If a clear comes with in a time window
   then no alarm is shown to the monitoring user.   
 */
public class AlarmMonitor {
   
    private static final long PAUSE = 5000;
    private static final double TEMP_STD = 20;
    private static final double TEMP_MEAN = 80;

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // setting Parallelism to 1 
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // Input stream of alarm events, event creation time is take as timestamp
        // Setting the Watermark to same as creation time of the event.
        DataStream<AlarmEvent> inputEventStream = env
                .addSource(new AlarmEventSource(PAUSE, TEMP_STD, TEMP_MEAN))
                .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks<AlarmEvent>() {

        			@Override
        			public long extractTimestamp(AlarmEvent event, long currentTimestamp) {
        				return event.getEventTime();
        			}

        			@Override
        			public Watermark checkAndGetNextWatermark(AlarmEvent lastElement, long extractedTimestamp) {
        				return new Watermark(extractedTimestamp);
        			}

        		});
        
        //Continuously prints the input events
        inputEventStream.print();    

        // Wait for 3 seconds and then decide if the event is really a critical issue
        // in the network element, I have used larger pause time between the event
        // to simulate time-out
        Pattern<AlarmEvent, ?> alarmPattern = Pattern.<AlarmEvent>begin("first")
                .where(evt -> evt.getSeverity().getValue()==Severity.CRITICAL.getValue())
                .next("second")
                .where(evt -> evt.getSeverity().getValue()==Severity.CLEAR.getValue() )
                .within(Time.seconds(3));

        
        
        DataStream<Either<String, String>> result = CEP.pattern(inputEventStream, alarmPattern).
        		select(new PatternTimeoutFunction<AlarmEvent, String>() {
        			
        			@Override
    				public String timeout(Map<String, AlarmEvent> pattern, long timeoutTimestamp) throws Exception {
        				System.out.println("Timeout "+pattern);
    					return pattern.get("first").toString() + "";
    				}
        			
        		},new PatternSelectFunction<AlarmEvent, String>() {
        			public String select(Map<String, AlarmEvent> pattern) {
    					StringBuilder builder = new StringBuilder();

    					builder.append(pattern.get("first").toString());

    					return builder.toString();
    				}
        			
        		}); 

        env.execute("CEP monitoring job");
    }
}
