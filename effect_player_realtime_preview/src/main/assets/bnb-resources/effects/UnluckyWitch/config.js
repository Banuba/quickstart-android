// group_phy
// joint_tail_root	-> group_phy
// joint_tail_L_1	-> joint_tail_root
// joint_tail_L_2	-> joint_tail_L_1
// joint_tail_L_3	-> joint_tail_L_2
// joint_tail_L_4	-> joint_tail_L_3
// joint_tail_R_1	-> joint_tail_root
// joint_tail_R_2	-> joint_tail_R_1
// joint_tail_R_3	-> joint_tail_R_2
// joint_tail_R_4	-> joint_tail_R_3
// joint_tail_R_5	-> joint_tail_R_4
// joint_tail_R_1_	-> joint_tail_root
// joint_tail_R_3_	-> joint_tail_R_2
// joint_tail_R_5_	-> joint_tail_R_4
// joint_tail_L_5	-> joint_tail_L_4
// joint_tail_R_6	-> joint_tail_R_5
// joint_tail_L_6	-> joint_tail_L_5



var isVideoPlay = true;
var isMaskOpened = false;

var settings = {
	effectName: "UnluckyWitch"
};

var spendTime = 0;
var analytic = {
	spendTimeSec: 0
};

var tapsCount = 0;

function sendAnalyticsData() {
	var _analytic;
	analytic.spendTimeSec = Math.round(spendTime / 1000);
	_analytic = {
		"Event Name": "Effects Stats",
		"Effect Name": settings.effectName,
        "Spend Time": String(analytic.spendTimeSec),
        "Effect Action" : "Tap",
        "Action Count" : tapsCount
	};
	Api.print("sended analytic: " + JSON.stringify(_analytic));
	Api.effectEvent("analytic", _analytic);
}

function timeUpdate() {
    if (effect.lastTime === undefined) effect.lastTime = (new Date()).getTime();

    var now = (new Date()).getTime();
    effect.delta = now - effect.lastTime;
    if (effect.delta < 3000) { // dont count spend time if application is minimized
        spendTime += effect.delta;
    }
    effect.lastTime = now;
}

function onStop() {
	try {
		sendAnalyticsData();
	} catch (err) {
		Api.print(err);
	}
}

function onFinish() {
	try {
		sendAnalyticsData();
	} catch (err) {
		Api.print(err);
	}
}


var meshes = [
    [
        new Mesh("Cut.bsm2", 0),
        new Mesh("Glasses.bsm2", 1),
        new Mesh("Hair.bsm2", 2),
        new Mesh("physics.bsm2", 3),
        new Mesh("Hat.bsm2", 4),
        new Mesh("Morph.bsm2", 5),
    ],
    [
        new Mesh("Cut.bsm2", 0),
        new Mesh("Glasses.bsm2", 1),
        new Mesh("Hair_exploded.bsm2", 2),
        new Mesh("Pig.bsm2", 3),
        new Mesh("Morph.bsm2", 4),
    ],
    [
        new Mesh("Cut.bsm2", 0),
        new Mesh("Glasses.bsm2", 1),
        new Mesh("Hair.bsm2", 2),
        new Mesh("physics.bsm2", 3),
        new Mesh("Hat.bsm2", 4),
        new Mesh("Morph.bsm2", 5),
    ],
    [
        new Mesh("Cut.bsm2", 0),
        new Mesh("Glasses.bsm2", 1),
        new Mesh("Hair.bsm2", 2),
        new Mesh("physics.bsm2", 3),
        new Mesh("Hat.bsm2", 4),
        new Mesh("Morph.bsm2", 5),
        new Mesh("Rabbit.bsm2", 6)
    ],

];

var currSet = 0;

var makeups = [
    "Make_up.png",
    "make_unlucky_witch_2.png",
    "OldMan_normal.png",
    "make_unlucky_witch_2.png",
];

var softLights = [
    "soft_princess.png",
    "soft_princess.png",
    "OldMan_soft.png",
    "OldMan_soft.png"
];

var glassesTextures = [
    "glass_Base_Color.ktx",
    "glass_Base_Color.ktx",
    "OldMan_glass_Base_Color.ktx",
    "glass_Base_Color.ktx"
];

var isAnimPlay = false;

function Effect() {
    var self = this;

    this.init = function() {
        Api.meshfxMsg("spawn", 9, 0, "!glfx_FACE");
        Api.meshfxMsg("shaderVec4", 0, 0, currSet === 2 ? "1.0" : "0.0");
        spawnMeshes();
        applyMakeup();
        applySoft();
        applyPhysics();

        Api.playVideo("frx", true, 1);
        Api.playSound("music.ogg", true, 1);

        Api.showHint("Tap");
        timeOut(3000, function(){
            Api.hideHint();
        });

        Api.showRecordButton();
    };

    this.restart = function() {
        Api.meshfxReset();
        Api.stopSound("music.ogg");
        self.init();
    };

    this.faceActions = [timeUpdate];
    this.noFaceActions = [timeUpdate];

    this.videoRecordStartActions = [];
    this.videoRecordFinishActions = [];
    this.videoRecordDiscardActions = [this.restart];
}

function onTouchesBegan() {
    ++tapsCount;
    Api.hideHint();
    effectTrigger();
}

function effectTrigger() {
    if (!isAnimPlay) {
        Api.playSound("sfx_1.ogg", false, 1);
        playChangeEffect();

        timeOut(1000, changeSet);

        timeOut(2000, function () {
            isAnimPlay = false;
        });

        isAnimPlay = true;
    }
}

function changeSet() {
    deleteMeshes();
    currSet = currSet + 1 >= meshes.length ? 0 : currSet + 1;
    Api.meshfxMsg("shaderVec4", 0, 0, currSet === 2 ? "1.0" : "0.0");
    spawnMeshes();
    applyMakeup();
    applySoft();
}

function applyMakeup() {
    Api.meshfxMsg("tex", 9, 0, makeups[currSet]);
}

function applySoft() {
    Api.meshfxMsg("tex", 9, 1, softLights[currSet]);
}

function Mesh(name, id) {
    this.name = name;
    this.id = id;
}

Mesh.prototype.spawn = function() {
    Api.meshfxMsg("spawn", this.id, 0, this.name);
};

Mesh.prototype.delete = function() {
    Api.meshfxMsg("del", this.id);
};

function spawnMeshes() {
    meshes[currSet].forEach(function(mesh) {
        if (mesh.name === "physics.bsm2") {
            applyPhysics();
        } 

        mesh.spawn();

        if (mesh.name === "Glasses.bsm2") {
            Api.meshfxMsg("tex", mesh.id, 0, glassesTextures[currSet]);
        }
    });
}

function deleteMeshes() {
    meshes[currSet].forEach(function(mesh) {
        mesh.delete();
    });
}


function playChangeEffect() {
    Api.playVideo("foreground", false, 1);
}

function timeOut(delay, callback) {
	var timer = new Date().getTime();

	effect.faceActions.push(removeAfterTimeOut);
	effect.noFaceActions.push(removeAfterTimeOut);

	function removeAfterTimeOut() {
        var now = new Date().getTime();
			
        if (now >= timer + delay) {
            var idx = effect.faceActions.indexOf(removeAfterTimeOut);
            effect.faceActions.splice(idx, 1);
            idx = effect.noFaceActions.indexOf(removeAfterTimeOut);
            effect.noFaceActions.splice(idx, 1);
            callback();
        }
	}
}

function applyPhysics() {
    Api.meshfxMsg("dynGravity", 3, 0, "0 -700 0");
    Api.meshfxMsg("dynImass", 3, 0, "group_phy");
    Api.meshfxMsg("dynImass", 3, 0, "joint_tail_root");
    Api.meshfxMsg("dynImass", 3, 0, "joint_tail_L_1");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_L_2");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_L_3");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_L_4");
    Api.meshfxMsg("dynImass", 3, 0, "joint_tail_R_1");
    Api.meshfxMsg("dynImass", 3, 0, "joint_tail_R_2");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_R_3");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_R_4");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_R_5");
    Api.meshfxMsg("dynImass", 3, 0, "joint_tail_R_1_");
    Api.meshfxMsg("dynImass", 3, 0, "joint_tail_R_3_");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_R_5_");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_R_6");
    Api.meshfxMsg("dynImass", 3, 10, "joint_tail_L_6");
}

var effect = new Effect();
configure(effect);