# Garadget
Garadget (Connect) - SmartThings to Garadget Integration

Garadget-SmartThings integration

Author: Stuart Buchanan

/*********************************************************************************************

Setup time: approximately about 5 minutes

PREREQUISITES

Your Garadget Devices fully operational (and connected to wifi)
Your Garadget credentials (username/password)
Developer access to SmartThings (e.g. http://graph.api.smartthings.com/)
Location set for your ST account
Under the ST mobile app, click on the 3-horizontal lines- "hamburger"- menu in the upper right corner, and then the "gear'" icon to review your location.

Determine your shard, please consult this thread:
https://community.smartthings.com/t/faq-how-to-find-out-what-shard-cloud-slice-ide-url-your-account-location-is-on/53923

If you are on a different shard, you need to change the links below for your right shard. As an example, in North America,

e.g. replace https://graph.api.smartthings.com/ide/apps by https://graph-na02-useast1.api.smartthings.com/ide/apps

INSTALLATION STEPS

For those with GitHub integration you can add my repository
Namespace:	fuzzysb
Repository:	Garadget
Branch:	master

You need to update from Repo the Garadget (Connect) Smart app and the Garadget Device Type.

then select your SmartApp and then:

a) click the App Settings Button at the top right corner (in the code window)

b) click the OAuth link and then click on the Enable OAuth in Smart App Button

c) click the Update Button at the bottom left

d) Go back to the code window, and hit the "publish/for me" button at the top right corner

All Complete.

For those without Github integration please do the following steps

/*********************************************************************************************

1) Create new device Handlers

/*********************************************************************************************

a) Go to https://graph.api.smartthings.com/ide/devices

b) Hit the "+New Device Handler" at the top right corner

c) Hit the "From Code" tab on the left corner

d) Copy and paste the code from https://github.com/fuzzysb/Garadget/blob/master/devicetypes/fuzzysb/garadget.src/garadget.groovy

e) Hit the create button at the bottom

f) Hit the "publish/for me" button at the top right corner (in the code window)

/*********************************************************************************************

2) Create a Smart App (Garadget (Connect))

/*********************************************************************************************

a) Go to https://graph.api.smartthings.com/ide/apps

b) Hit the "+New SmartApp" at the top right corner

c) Hit the "From Code" tab on the left corner

d) Copy and paste the code from https://github.com/fuzzysb/Garadget/blob/master/smartapps/fuzzysb/garadget-connect.src/garadget-connect.groovy

e) Hit the create button at the bottom

f) Hit the "publish/for me" button at the top right corner (in the code window)

g) click the App Settings Button at the top right corner (in the code window)

h) click the OAuth link and then click on the Enable OAuth in Smart App Button

i) click the Update Button at the bottom left

g) Go back to the code window, and hit the "publish/for me" button at the top right corner

/*********************************************************************************************

3) Connect Smartthings to Garadget

/*********************************************************************************************

You should already have an Garadget username and password, if not go to the Garadget application and create a new login

Go through the authentication process using Garadget (Connect)

If you get a blank screen after pressing 'Next or you get the following error: " Error - bad state. Unable to complete page configuration", you'd need to enable oAuth as specified in step 2h) above.

After being connected, click 'Next' and select your Garadget device(s) that you want to control from Smartthings and, then press 'Next'

once complete you now have devices that have been created for each of the devices you selected during setup, you should enter the Garadget (Connect) smartapp to add or delete these devices.

/*********************************************************************************************

4) Your device(s) should now be ready to process your commands

/*********************************************************************************************

You should see your device under

https://graph.api.smartthings.com/device/list

And

In the ST app, under myHome/Things.

countless hours have been devoted to developing this smartapp and connected devices. if you use and find useful please donate to aid further development of this product. any and all donations are very much appreciated.

https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CNRR3ER3CTYDQ

