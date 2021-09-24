
function Effect()
{
    this.init = function() {
        Api.showRecordButton();
    };
    this.faceActions = [];
    this.noFaceActions = [];
    this.videoRecordStartActions = [];
    this.videoRecordFinishActions = [];
    this.videoRecordDiscardActions = [function() {
        Api.showRecordButton();
    }];
}

configure(new Effect());
