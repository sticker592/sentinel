let SwixDetailsModel = require('../models/swixer.model');


let insertSwixDetails = (swixDetails, cb) => {
  swixDetails = new SwixDetailsModel(swixDetails);
  swixDetails.save((error, result) => {
    if (error) cb(error, null);
    else cb(null, result);
  });
};

let getSwix = (swixHash, cb) => {
  SwixDetailsModel.findOne({
    swixHash
  }, {
    _id: 0
  }, (error, result) => {
    if(error) cb(error, null);
    else cb(null, result);
  });
};

let getValidSwixes = (cb) => {
  SwixDetailsModel.find({
    $or: [
      {
        'remainingAmount': {
          $exists: false
        }
      },
      {
        'remainingAmount': {
          $gt: 0
        }
      }
    ],
    'tries': {
      $lt: 10
    }
  }, {
      '_id': 0
    }, (error, result) => {
      if (error) cb(error, null);
      else cb(null, result || []);
    });
};

let updateSwixStatus = (toAddress, message, cb) => {
  SwixDetailsModel.findOneAndUpdate({
    toAddress
  }, {
      $set: {
        message,
        'lastUpdateOn': Math.round(Date.now() / Math.pow(10, 3))
      }
    }, (error, result) => {
      if (error) cb(error, null);
      else cb(null, result);
    });
};

let increaseTries = (toAddress, cb) => {
  SwixDetailsModel.findOneAndUpdate({
    toAddress
  }, {
      $inc: {
        'tries': 1
      }
    }, (error, result) => {
      if (error) cb(error, null);
      else cb(null, result);
    });
};

let updateSwixTransactionStatus = (toAddress, txInfo, remainingAmount, cb) => {
  SwixDetailsModel.findOneAndUpdate({
    toAddress
  }, {
      $push: {
        'txInfos': txInfo
      },
      $set: {
        remainingAmount
      }
    }, (error, result) => {
      if (error) cb(error, null);
      else cb(null, result);
    });
};

module.exports = {
  insertSwixDetails,
  getSwix,
  getValidSwixes,
  updateSwixStatus,
  increaseTries,
  updateSwixTransactionStatus
};
