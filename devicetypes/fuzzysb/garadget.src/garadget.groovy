/**
 *  Garadget Device Handler
 *
 *  Copyright 2016 Stuart Buchanan based loosely based on original code by Krishnaraj Varma with thanks
 *  Additional contribution by:
 *   - RBoy
 *   - btrenbeath
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * 21/03/2018 V1.8 Report Door Control and Garage Door Control state -RBoy
 * 14/02/2018 V1.7 Support Door Control capability for expanded compatibility with SmartApps -RBoy
 * 04/02/2018 V1.6 Updated Door Status response parsing to be more resilient to changes in format from Garadget, and fixed bug resulting from latest Garadget response format change. -btrenbeath
 * 12/12/2017 V1.5 UPDATED - Changed the route to immediately update send the 'Closing' status regardless of what the Garget device reads. Also changed the mechanism used to wait on the 'opening' delay to utilize 'runOnce' instead of 'delayBetween', since delayBetween is broken in the SmartThings SDK.  I added 2000 miliseconds to the wait time configured in Garadget for the device to account for lag and delay in SmartThings execution. - btrenbeath
 * 22/07/2016 V1.4 updated with "Garage Door Control" capability with thanks to Nick Jones, also have improved the open command to refresh status again after door motion timeframe has elapsed
 * 12/02/2016 V1.3 updated with to remove token and DeviceId parameters from inputs to retrieving from dni
 */


import groovy.json.JsonOutput

preferences {
	input("prdt", "text", title: "sensor scan interval in mS (default: 1000)")
	input("pmtt", "text", title: "door moving time in mS(default: 10000)")
	input("prlt", "text", title: "button press time mS (default: 300)")
	input("prlp", "text", title: "delay between consecutive button presses in mS (default: 1000)")
	input("psrr", "text", title: "number of sensor reads used in averaging (default: 3)")
	input("psrt", "text", title: "reflection threshold below which the door is considered open (default: 25)")
	input("paot", "text", title: "alert for open timeout in seconds (default: 320)")
	input("pans", "text", title: " alert for night time start in minutes from midnight (default: 1320)")
	input("pane", "text", title: " alert for night time end in minutes from midnight (default: 360)")
}

metadata {
	definition (name: "Garadget", namespace: "fuzzysb", author: "Stuart Buchanan") {

    capability "Switch"
		capability "Contact Sensor"
    capability "Signal Strength"
		capability "Actuator"
		capability "Sensor"
    capability "Refresh"
    capability "Polling"
		capability "Configuration"
		capability "Garage Door Control"
		capability "Door Control"

    attribute "reflection", "string"
    attribute "status", "string"
    attribute "time", "string"
    attribute "lastAction", "string"
    attribute "reflection", "string"
    attribute "ver", "string"

    command "stop"
		command "statusCommand"
		command "setConfigCommand"
		command "doorConfigCommand"
		command "netConfigCommand"
	}

	simulator {

	}

tiles(scale: 2) {
    multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
        tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "open", label:'${name}', action:"switch.off", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e"
            attributeState "opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffa81e"
            attributeState "closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#6699ff"
            attributeState "closed", label:'${name}', action:"switch.on", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821"
        }
		tileAttribute ("device.lastAction", key: "SECONDARY_CONTROL") {
				attributeState "default", label: 'Time In State: ${currentValue}'
		}
    }
    standardTile("contact", "device.contact", width: 1, height: 1) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
    valueTile("reflection", "reflection", decoration: "flat", width: 2, height: 1) {
    		state "reflection", label:'Reflection\r\n${currentValue}%'
		}
    valueTile("rssi", "device.rssi", decoration: "flat", width: 1, height: 1) {
    		state "rssi", label:'Wifi\r\n${currentValue} dBm', unit: "",backgroundColors:[
            		[value: 16, color: "#5600A3"],
					[value: -31, color: "#153591"],
					[value: -44, color: "#1e9cbb"],
					[value: -59, color: "#90d2a7"],
					[value: -74, color: "#44b621"],
					[value: -84, color: "#f1d801"],
					[value: -95, color: "#d04e00"],
					[value: -96, color: "#bc2323"]
				]
		}
    standardTile("refresh", "refresh", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
    standardTile("stop", "stop") {
        	state "default", label:"", action: "stop", icon:"http://cdn.device-icons.smartthings.com/sonos/stop-btn@2x.png"
        }
    valueTile("ip", "ip", decoration: "flat", width: 2, height: 1) {
    		state "ip", label:'IP Address\r\n${currentValue}'
		}
    valueTile("ssid", "ssid", decoration: "flat", width: 2, height: 1) {
    		state "ssid", label:'Wifi SSID\r\n${currentValue}'
		}
    valueTile("ver", "ver", decoration: "flat", width: 1, height: 1) {
    		state "ver", label:'Version\r\n${currentValue}'
		}
    standardTile("configure", "device.button", width: 1, height: 1, decoration: "flat") {
        	state "default", label: "", backgroundColor: "#ffffff", action: "configure", icon:"st.secondary.configure"
		}

    main "status"
		details(["status", "contact", "reflection", "ver", "configure", "lastAction", "rssi", "stop", "ip", "ssid", "refresh"])
	}
}

// handle commands
def poll() {
	log.debug "Executing 'poll' - Device Manager poll"
    refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()
    netConfigCommand()
    doorConfigCommand()

}

def configure() {
	log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	SetConfigCommand()
}

// Parse incoming device messages to generate events
private parseDoorStatusResponse(resp) {
    log.debug("Executing parseDoorStatusResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        log.debug("returnedresult: "+resp.data.result)
        def results = (resp.data.result).tokenize('|')
        def statusvalues = (results[0]).tokenize('=')
        def timevalues = (results[1]).tokenize('=')
        def sensorvalues = (results[2]).tokenize('=')
        def signalvalues = (results[3]).tokenize('=')
        def status = statusvalues[1]
        sendEvent(name: 'status', value: status)
        if(status == "open" || status == "closed"){
        	sendEvent(name: 'contact', value: status, displayed: false)
		sendEvent(name: 'door', value: status, displayed: false)
            }
        def time = timevalues[1]
        sendEvent(name: 'lastAction', value: time)
        def sensor = sensorvalues[1]
        sendEvent(name: 'reflection', value: sensor)
        def signal = signalvalues[1]
        sendEvent(name: 'rssi', value: signal)

    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseDoorConfigResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        log.debug("returnedresult: "+resp.data.result)
        def results = (resp.data.result).tokenize('|')

    		results.each { value ->
            	def resultValue = value.tokenize('=')
                switch (resultValue[0]) {
                	case "ver": def ver = resultValue[1];
                    			log.debug ("GARADGET: Firmware Version (ver): " +ver);
                    			sendEvent(name: 'ver', value: ver, displayed: false);
                                break;

                    case "rdt": def rdt = resultValue[1];
                    			log.debug ("GARADGET: Sensor Scan Interval (rdt): " +rdt);
                                break;

                    case "mtt": def mtt = resultValue[1];
                    			state.mtt = mtt;
                                log.debug ("GARADGET: Door Moving Time (mtt): " +mtt);
                                break;

                    case "rlt": def rlt = resultValue[1];
                    			log.debug ("GARADGET: Button Press time (rlt): " +rlt);
                                break;

                    case "rlp": def rlp = resultValue[1];
                    			log.debug ("GARADGET: Delay Between Consecutive Button Presses (rlp)" +rlp);
                                break;

                    case "srr": def srr = resultValue[1];
                    			log.debug ("GARADGET: Number of Sensor Feeds used in Averaging: " +srr);
                                break;

                    case "srt": def srt = resultValue[1];
                    			log.debug ("GARADGET: Reflection Value below which door is 'open': " +srt);
                                break;

                    case "aot": def aot = resultValue[1];
                    			log.debug ("GARADGET: Alert for Open Timeout in seconds: " +aot);
                                break;

                    case "ans": def ans = resultValue[1];
                    			log.debug ("GARADGET: Alert for night time start in minutes from midnight: " +ans);
                                break;

                    case "ane": def ane = resultValue[1];
                    			log.debug ("GARADGET: Alert for night time end in minutes from midnight: " +ane );
                                break;

                    default : log.debug ("GARADGET UNUSED CONFIG: " +resultValue[0] +" value of " +resultValue[1])
                }
            }


    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseNetConfigResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        log.debug("returnedresult: "+resp.data.result)
        def results = (resp.data.result).tokenize('|')
        def ipvalues = (results[0]).tokenize('=')
        def snetvalues = (results[1]).tokenize('=')
        def dgwvalues = (results[2]).tokenize('=')
        def macvalues = (results[3]).tokenize('=')
        def ssidvalues = (results[4]).tokenize('=')
        def ip = ipvalues[1]
        sendEvent(name: 'ip', value: ip, displayed: false)
        log.debug("IP Address: "+ip)
        def snet = snetvalues[1]
        log.debug("Subnet Mask: "+snet)
        def dgw = dgwvalues[1]
        log.debug("Default Gateway: "+dgw)
        def mac = macvalues[1]
        log.debug("Mac Address: "+mac)
        def ssid = ssidvalues[1]
        sendEvent(name: 'ssid', value: ssid, displayed: false)
        log.debug("Wifi SSID : "+ssid)
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def id = resp.data.id
        def name = resp.data.name
        def connected = resp.data.connected
		    def returnValue = resp.data.return_value
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private getDeviceDetails() {
  def fullDni = device.deviceNetworkId
  return fullDni
}

private sendCommand(method, args = []) {
	def DefaultUri = "https://api.particle.io"
  def cdni = getDeviceDetails().tokenize(':')
	def deviceId = cdni[0]
	def token = cdni[1]
    def methods = [
		'doorStatus': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/doorStatus",
					requestContentType: "application/json",
					query: [access_token: token]
                    ],
    'doorConfig': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/doorConfig",
					requestContentType: "application/json",
					query: [access_token: token]
                    ],
		'netConfig': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/netConfig",
					requestContentType: "application/json",
					query: [access_token: token]
                   	],
		'setState': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/setState",
					requestContentType: "application/json",
                    query: [access_token: token],
					body: args[0]
                   	],
		'setConfig': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/setConfig",
					requestContentType: "application/json",
                    query: [access_token: token],
					body: args[0]
                   	]
	]

	def request = methods.getAt(method)

    log.debug "Http Params ("+request+")"

    try{
        log.debug "Executing 'sendCommand'"

        if (method == "doorStatus"){
            log.debug "calling doorStatus Method"
            httpGet(request) { resp ->
                parseDoorStatusResponse(resp)
            }
        }else if (method == "doorConfig"){
            log.debug "calling doorConfig Method"
            httpGet(request) { resp ->
                parseDoorConfigResponse(resp)
            }
        }else if (method == "netConfig"){
			      log.debug "calling netConfig Method"
            httpGet(request) { resp ->
                parseNetConfigResponse(resp)
            }
        }else if (method == "setState"){
            log.debug "calling setState Method"
            httpPost(request) { resp ->
                parseResponse(resp)
			      }
        }else if (method == "setConfig"){
            log.debug "calling setConfig Method"
            httpPost(request) { resp ->
                 parseResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        log.debug("___exception: " + e)
    }
}


def on() {
/* UPDATED - Changed the route to immediately update send the 'Opening' status regardless of what the Garget device reads. */
/* Also changed the mechanism used to wait on the 'opening' delay to utilize 'runOnce' instead of 'delayBetween', since    */
/* delayBetween is broken in the SmartThings SDK.  I added 2000 miliseconds to the wait time configured in Garadget */
/* for the device to account for lag and delay in SmartThings execution. - btrenbeath   */

	log.debug ("Executing - on()")
	openCommand()

    def buttonPressTime =  new Date()
    def myDelay = (state.mtt).toInteger()
    def laterTime = new Date(now() + myDelay + 2000)
   	log.debug ("TimeStamp - on() - Now: "+buttonPressTime)
    log.debug ("Paramater Validation - on() - Garage Open time - myDelay")
    log.debug ("TimeStamp - on() - +15: "+laterTime)

 	sendEvent(name: 'status', value: 'opening')
	sendEvent(name: 'door', value: 'opening')
    log.debug ("Executing - on() - SendEvent opening")

    runOnce(laterTime, statusCommand, [overwrite: false])
    log.debug ("Executing - on() - runOnce(statusCommand)")
}

def off() {
/* UPDATED - Changed the route to immediately update send the 'Closing' status regardless of what the Garget device reads. */
/* Also changed the mechanism used to wait on the 'opening' delay to utilize 'runOnce' instead of 'delayBetween', since    */
/* delayBetween is broken in the SmartThings SDK.  I added 2000 miliseconds to the wait time configured in Garadget */
/* for the device to account for lag and delay in SmartThings execution. - btrenbeath   */

	log.debug ("Executing - off()")
	closeCommand()

    def buttonPressTime =  new Date()
    def myDelay = (state.mtt).toInteger()
    def laterTime = new Date(now() + myDelay + 2000)
   	log.debug ("TimeStamp - off() - Now: "+buttonPressTime)
    log.debug ("Paramater Validation - off() - Garage Open time - myDelay")
    log.debug ("TimeStamp - off() - +15: "+laterTime)

    sendEvent(name: 'status', value: 'closing')
    sendEvent(name: 'door', value: 'closing')
    log.debug ("Executing - off() - SendEvent closing")

    runOnce(laterTime, statusCommand, [overwrite: false])
    log.debug ("Executing - off() - runOnce(statusCommand)")
}

def stop(){
	log.debug "Executing - stop() - 'sendCommand.setState'"
  def jsonbody = new groovy.json.JsonOutput().toJson(arg:"stop")

  sendCommand("setState",[jsonbody])
  statusCommand()
}

def statusCommand(){
	log.debug "Executing - statusCommand() - 'sendCommand.statusCommand'"
	sendCommand("doorStatus",[])
}

def openCommand(){
	log.debug "Executing - openCommand() - 'sendCommand.setState'"
  def jsonbody = new groovy.json.JsonOutput().toJson(arg:"open")
	sendCommand("setState",[jsonbody])
}

def closeCommand(){
	log.debug "Executing - closeCommand() - 'sendCommand.setState'"
	def jsonbody = new groovy.json.JsonOutput().toJson(arg:"close")
	sendCommand("setState",[jsonbody])
}

def open() {
	log.debug "Executing - open() - 'on'"
	on()
}

def close() {
	log.debug "Executing - close() - 'off'"
	off()
}

def doorConfigCommand(){
	log.debug "Executing doorConfigCommand() - 'sendCommand.doorConfig'"
	sendCommand("doorConfig",[])
}

def SetConfigCommand(){
	def crdt = prdt ?: 1000
	def cmtt = pmtt ?: 10000
	def crlt = prlt ?: 300
	def crlp = prlp ?: 1000
	def csrr = psrr ?: 3
	def csrt = psrt ?: 25
	def caot = paot ?: 320
	def cans = pans ?: 1320
	def cane = pane ?: 360
	log.debug "Executing 'sendCommand.setConfig'"
	def jsonbody = new groovy.json.JsonOutput().toJson(arg:"rdt=" + crdt +"|mtt=" + cmtt + "|rlt=" + crlt + "|rlp=" + crlp +"|srr=" + csrr + "|srt=" + csrt)
	sendCommand("setConfig",[jsonbody])
    jsonbody = new groovy.json.JsonOutput().toJson(arg:"aot=" + caot + "|ans=" + cans + "|ane=" + cane)
    sendCommand("setConfig",[jsonbody])
}

def netConfigCommand(){
	log.debug "Executing 'sendCommand.netConfig'"
	sendCommand("netConfig",[])
}
