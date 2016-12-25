import os
import subprocess
import uuid
import omxcontrol
import json
import time
from flask import Flask,request,make_response,render_template_string
app = Flask(__name__)

SERVER_CODE_LIST = 1
SERVER_CODE_DURATION = 2
SERVER_CODE_POSITION = 3
SERVER_CODE_PLAYBACK = 4

ROOT_DIR = '/media/pi'
users = {}

@app.route('/')
def hello():
    print 'user added'
    user = str(uuid.uuid4())
    users[user] = ROOT_DIR
    print user,'\n'
    
    resp = make_response(render_template_string('Hello'))
    resp.set_cookie('user',user)
    return resp

'''#check state of server
@app.route("/getcurdir")
def get_cur_dir():
    return cur_dir '''     
    
@app.route("/killall")
def killall():
    proc = subprocess.Popen('killall omxplayer.bin',shell=True)
    #proc = subprocess.Popen('pkill -f "dbus-daemon --fork --print-address"',shell=True)
    return "" 
    
@app.route("/app")
def somefunc():
    global users
    user = request.cookies.get('user')
    if user not in users:
        response = make_response(render_template_string('no id'), 401)
        return response
    arg = request.args.get('arg','')
    if arg == '..':
        if users[user] == ROOT_DIR:
            path = ROOT_DIR
        else:
            path = os.path.dirname(users[user])
    else:
        path = os.path.join(users[user],arg)
    if os.path.isfile(path):
        proc = subprocess.Popen('omxplayer -b --aspect-mode stretch "%s"' % path,stdout=subprocess.PIPE,shell=True)
        try:
            #omx = omxcontrol.OmxControl()
            #duration = omx.duration()
            return json.dumps({"code":SERVER_CODE_PLAYBACK},ensure_ascii=False)
        except omxcontrol.OmxControlError:
            return ''
    elif os.path.isdir(path):
        proc = subprocess.Popen('ls "%s"' % path,stdout=subprocess.PIPE,shell=True)
        out,err = proc.communicate()
        out = out.decode('utf-8')
        users[user] = path
        print 'sending ', out
        #get client to know there's a string for it
        return json.dumps({'code':SERVER_CODE_LIST,'list':out},ensure_ascii=False)
    
    return ''

@app.route('/pause')
def pause():
    subprocess.Popen('/home/pi/omxplayer-master/dbuscontrol.sh pause',shell=True)
    return '' 
    
@app.route('/volumeup')
def volumeup():
    subprocess.Popen('/home/pi/omxplayer-master/dbuscontrol.sh volumeup',shell=True)
    return '' 
    
@app.route('/volumedown')
def volumedown():
    subprocess.Popen('/home/pi/omxplayer-master/dbuscontrol.sh volumedown',shell=True)
    return ''
    
@app.route('/togglesubtitles')
def togglesubtitles():
    subprocess.Popen('/home/pi/omxplayer-master/dbuscontrol.sh togglesubtitles',shell=True)
    return ''
    
@app.route('/setposition')
def setposition():
    arg = request.args.get('arg','')
    print arg
    if arg == '':
        return ''
    subprocess.Popen('/home/pi/omxplayer-master/dbuscontrol.sh setposition %s' % arg,shell=True)
    return '' 

@app.route('/duration')
def getDuration():
    try:
        omx = omxcontrol.OmxControl()
        duration = omx.duration()
        return json.dumps({'code':SERVER_CODE_DURATION,'duration':int(duration.seconds)},ensure_ascii=False)
    except omxcontrol.OmxControlError,e:
        print str(e)
        return ''
 


if __name__ == "__main__":
    app.run(host='0.0.0.0',port=5000)
