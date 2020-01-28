
function Effect()
{
    var self = this;

    self.loveDelay = 1350;
    self.jumpDelayStart = 1000;
    self.jumpDelay = 700;

    self.fallDownDelay = 500;

    /*
	this.meshes = [
		{ file:"octopus.bsm2", anims:[
			{ a:"start", t:1400 },
			{ a:"idle", t:1733.33 },
			{ a:"trigger_1", t:1700 },
			{ a:"trigger_2_start", t:2500 },
			{ a:"trigger_2_idle", t:1966.67 },
			{ a:"trigger_2_end", t:933.333 },
			{ a:"trigger_3", t:2166.67 },
		] },
	];
	*/

    this.soundJump = function() {
        var now = (new Date()).getTime();
        if (now > self.soundT) {
            Api.playSound("Octopus_Jump.ogg", false, 1);
            self.faceActions = [self.play];
        }
    };

    this.soundFallDown = function() {
        var now = (new Date()).getTime();

        if (now >= self.fallDownTime) {
            Api.playSound("fall_down_to_ear.ogg", false, 1);
            self.fallDownTime = Number.MAX_VALUE;
        }

        if (now > self.soundT) {
            Api.playSound("Octopus_Love_Hearts.ogg", false, 1);
            self.faceActions = [self.play];
        }
    };

    this.play = function() {
        var now = (new Date()).getTime();

        if (now < self.t)
            return;

        if (self.onTop) {
            if (isMouthOpen(world.landmarks, world.latents)) {
                Api.hideHint();
                if (Math.random() < 0.5) {
                    Api.meshfxMsg("animOnce", 0, 0, "trigger_1");
                    Api.meshfxMsg("animLoop", 0, 1, "idle");
                    Api.playSound("octopus_hello.ogg", false, 1);
                    self.t = now + 1700;
                } else {
                    Api.meshfxMsg("animOnce", 0, 0, "trigger_3");
                    Api.meshfxMsg("animLoop", 0, 1, "idle");
                    Api.playSound("Long_talking.ogg", false, 1);
                    self.t = now + 2166.67;
                }
            } else if (isSmile(world.landmarks, world.latents)) {
                Api.hideHint();
                Api.meshfxMsg("animOnce", 0, 0, "trigger_2_start");
                Api.meshfxMsg("animLoop", 0, 1, "trigger_2_idle");

                self.fallDownTime = now + self.fallDownDelay;
                self.t = now + 2500;
                self.soundT = now + self.loveDelay;
                self.faceActions = [self.soundFallDown];
                self.onTop = false;
            }
        } else {
            if (isSmile(world.landmarks, world.latents)) {
                Api.meshfxMsg("animOnce", 0, 0, "trigger_2_end");
                Api.meshfxMsg("animLoop", 0, 1, "idle");
                Api.playSound("jump_from_ear.ogg", false, 1);
                self.t = now + 933.333;
                self.soundT = now + self.jumpDelay;
                self.faceActions = [self.soundJump];
                self.onTop = true;
            }
        }
    };

    this.init = function() {
        Api.meshfxMsg("spawn", 1, 0, "!glfx_FACE");

        Api.meshfxMsg("spawn", 0, 0, "octopus.bsm2");
        Api.meshfxMsg("animOnce", 0, 0, "start");
        Api.meshfxMsg("animLoop", 0, 1, "idle");
        Api.playSound("Octopus_Soundfont_Strings.ogg", true, 1);
        Api.playSound("Octopus_Intro.ogg", false, 1);

        self.fallDownTime = Number.MAX_VALUE;
        self.t = 0;
        self.soundT = (new Date()).getTime() + self.jumpDelayStart;
        self.faceActions = [self.soundJump];
        self.onTop = true;

        Api.showHint("Smile or open mouth");
        Api.playVideo("frx", true, 1);
        Api.showRecordButton();
    };

    this.restart = function() {
        Api.meshfxReset();
        Api.stopVideo("frx");
        // Api.stopSound("sfx.aac");
        self.init();
    };

    this.faceActions = [];
    this.noFaceActions = [];

    this.videoRecordStartActions = [this.restart];
    this.videoRecordFinishActions = [];
    this.videoRecordDiscardActions = [this.restart];
}

configure(new Effect());
