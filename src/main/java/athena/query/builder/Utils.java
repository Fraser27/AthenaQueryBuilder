package athena.query.builder;
/*
 * Copyright (C) 2020 ATHENA Query DSL AUTHOR; Fraser Sequeira
 * All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 **/
import java.util.List;
import java.util.Map;

/**
 * Class holds helper functions for re-usability.
 * 
 * @author fraser.sequeira
 * 
 * */
public class Utils {

	public static boolean isEmpty(List list) {
		return list == null || list.size() == 0;
	}
	
	public static boolean isEmpty(Map map) {
		return map == null || map.size() > 0;
	}
	
	public static boolean notEmpty(List list) {
		return list != null && list.size() > 0;
	}
	
	public static boolean notEmpty(Map map) {
		return map != null && map.size() > 0;
	}
}
