const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}')
    .onWrite(event => {

        const user_id = event.params.user_id;
        const notification_id = event.params.notification_id;

        console.log('New notification to be sent to ID: ', user_id);

        if(!event.data.val()){
            return console.log('Notification deleted, ID: ', notification_id)
        }

        const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
        return fromUser.then(fromUserResult =>{
            const fromUserId = fromUserResult.val().from;

            const userQuery = admin.database().ref(`users/${fromUserId}/name`).once('value');
            return userQuery.then(userResult =>{

                const fromUserName = userResult.val();

                const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');
                return deviceToken.then(result =>{

                    const token_id = result.val();

                    const payload = {
                        notification: {
                            title : "New Friend Request",
                            body: `${fromUserName} sent you friend request`,
                            icon: "default",
                            click_action: 'com.soumya.slimechat_TARGET_NOTIFICATION'
                        },
                        data: {
                            from_user_id : fromUserId
                        }
                    };

                    return admin.messaging().sendToDevice(token_id, payload).then(response => {
                        console.log('Notification sent, ID', notification_id);

                    });

                  });

                });

            });


    });
