function Effect() {
    var self = this;
    this.play = function() {
        now = (new Date()).getTime();
        if (now > self.time) {
            Api.hideHint();
            self.faceActions = [];
        }
        if(Api.isMouthOpen()) {
            Api.hideHint();
            self.faceActions = [];
        };
    };

    this.init = function() {
        Api.meshfxMsg("spawn", 2, 0, "!glfx_FACE");
        Api.meshfxMsg("spawn", 0, 0, "Trollma_morphing.bsm2");
        Api.meshfxMsg("spawn", 1, 0, "TrollGrandma.bsm2");
        if(Api.getPlatform() == "iOS"){
            Api.showHint("Voice changer");
        };
        self.time = (new Date()).getTime() + 3000;
        self.faceActions = [self.play];
        Api.playSound("music.m4a",true,1);
        Api.showRecordButton();
    };
    this.restart = function() {
        Api.meshfxReset();
        self.init();
    };
    this.stopSound = function () {
        if(Api.getPlatform() == "ios") {
            Api.hideHint();
            Api.stopSound("music.m4a");                  
        };
    };
    this.faceActions = [];
    this.noFaceActions = [];
    this.videoRecordStartActions = [self.stopSound];
    this.videoRecordFinishActions = [];
    this.videoRecordDiscardActions = [this.restart];
}
configure(new Effect());