
// created by KotlinWriter.kt on Wed Sep 18 18:41:42 BST 2019
package com.typedpath.serverless


class S3Put {
    var records :  List<Items>?  = null


    class Items  {
        var awsRegion :  String?  = null
        var eventName :  String?  = null
        var eventSource :  String?  = null
        var eventTime :  String?  = null
        var eventVersion :  String?  = null
        var requestParameters :  RequestParameters?  = null
        var responseElements :  ResponseElements?  = null
        var s3 :  S3?  = null
        var userIdentity :  UserIdentity?  = null


        class RequestParameters  {
            var sourceIPAddress :  String?  = null




        }
        class ResponseElements  {
            var xAmzId2 :  String?  = null
            var xAmzRequestId :  String?  = null




        }
        class S3  {
            var bucket :  Bucket?  = null
            var configurationId :  String?  = null
            var object_ :  Object?  = null
            var s3SchemaVersion :  String?  = null


            class Bucket  {
                var arn :  String?  = null
                var name :  String?  = null
                var ownerIdentity :  OwnerIdentity?  = null


                class OwnerIdentity  {
                    var principalId :  String?  = null




                }


            }
            class Object  {
                var eTag :  String?  = null
                var key :  String?  = null
                var sequencer :  String?  = null
                var size :  String?  = null




            }


        }
        class UserIdentity  {
            var principalId :  String?  = null




        }


    }


}
