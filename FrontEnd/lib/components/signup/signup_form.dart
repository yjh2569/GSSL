import 'dart:io';

import 'package:GSSL/api/api_signup.dart';
import 'package:GSSL/model/request_models/signup.dart';
import 'package:GSSL/model/response_models/general_response.dart';
import 'package:GSSL/pages/login_page.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:image_picker/image_picker.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../constants.dart';
import '../util/custom_dialog.dart';

class SignUpForm extends StatefulWidget {
  const SignUpForm({
    Key? key,
  }) : super(key: key);

  @override
  State<SignUpForm> createState() => _SignupFormState();
}

class _SignupFormState extends State<SignUpForm> {
  final SignupFormKey = GlobalKey<FormState>();

  String? member_id = '';
  String? password = '';
  String? nickname = '';
  String? gender = 'M';
  String? phone = '';
  String? email = '';
  XFile? profileImage;
  String? introduce = '';
  generalResponse? signup;
  ApiSignup apiSignup = ApiSignup();
  bool showTerms = false;
  bool agreed = false;

  bool checkDupId = false;
  bool checkDupNickname = false;

  final picker = ImagePicker();

  Future<void> chooseImage() async {
    var choosedimage = await picker.pickImage(source: ImageSource.gallery);
    //set source: ImageSource.camera to get image from camera
    setState(() {
      profileImage = choosedimage;
    });
  }

  @override
  void initState() {
    super.initState();
  }

  bool _submitted = false;

  void _submit() async {
    // set this variable to true when we try to submit
    setState(() => _submitted = true);
    if (checkDupId &&
        checkDupNickname &&
        SignupFormKey.currentState!.validate()) {
      SignupFormKey.currentState!.save();
      generalResponse result = await apiSignup.signup(
          profileImage,
          Signup(
              memberId: member_id,
              password: password,
              nickname: nickname,
              gender: gender,
              phone: phone,
              email: email,
              introduce: introduce));
      if (result.statusCode == 201) {
        showDialog(
            context: context,
            builder: (BuildContext context) {
              return CustomDialog("??????????????? ??????????????????.", (context) => LoginScreen());
            });
      } else {
        showDialog(
            context: context,
            builder: (BuildContext context) {
              return CustomDialog(result.message!, null);
            });
      }
    } else {
      showDialog(
          context: context,
          builder: (BuildContext context) {
            return CustomDialog("?????? ????????? ??????????????????.", null);
          });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: SignupFormKey,
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(0, 25, 0, 0),
            child: TextFormField(
              // ?????????
              style: TextStyle(fontFamily: "Sub"),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,
              cursorColor: btnColor,
              onChanged: (val) {
                member_id = val;
                setState(() => checkDupId = false);
              },
              autovalidateMode: _submitted
                  ? AutovalidateMode.onUserInteraction
                  : AutovalidateMode.disabled,
              validator: (text) {
                if (text == null || text.isEmpty) {
                  return '???????????? ??????????????????.';
                }
                if (!RegExp(r"^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{5,20}$")
                    .hasMatch(text)) {
                  return '????????? ????????? ????????? 5 ~ 20?????? ??????????????????.';
                }
                if (!checkDupId) {
                  return '????????? ??????????????? ????????????.';
                }
                return null;
              },
              decoration: InputDecoration(
                errorStyle: TextStyle(fontFamily: "Sub"),
                isCollapsed: true,
                hintText: "?????????",
                hintStyle: TextStyle(color: sColor, fontFamily: "Sub"),
                contentPadding: EdgeInsets.fromLTRB(20.w, 10.h, 10.w, 10.h),
                enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: sColor)),
                filled: true,
                fillColor: Colors.white,
                focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: btnColor)),
                suffix: ElevatedButton(
                  onPressed: () async {
                    // ????????? ????????????
                    print(member_id);
                    setState(() => _submitted = true);
                    if (member_id == null ||
                        member_id!.isEmpty ||
                        !RegExp(r"^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{5,20}$")
                            .hasMatch(member_id!)) {
                      return;
                    }
                    generalResponse result = await apiSignup.checkId(member_id);
                    print(result.statusCode);
                    print(result.message);
                    if (result?.statusCode == 200) {
                      setState(() => checkDupId = true);
                      showDialog(
                          context: context,
                          builder: (BuildContext context) {
                            return CustomDialog("?????? ????????? ??????????????????.", null);
                          });
                    } else {
                      setState(() => checkDupId = false);
                      showDialog(
                          context: context,
                          builder: (BuildContext context) {
                            return CustomDialog(result.message!, null);
                          });
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    padding: EdgeInsets.all(0),
                    backgroundColor: btnColor,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(5.0),
                    ),
                    tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                  ),
                  child: Text(
                    "????????????".toUpperCase(),
                    style: TextStyle(
                      fontFamily: "Sub",
                    ),
                  ),
                ),
              ),
            ),
          ),
          Padding(
            // ????????????
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: TextFormField(
              style: TextStyle(fontFamily: "Sub"),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,
              obscureText: true,
              cursorColor: btnColor,
              onChanged: (val) {
                password = val;
              },
              autovalidateMode: _submitted
                  ? AutovalidateMode.onUserInteraction
                  : AutovalidateMode.disabled,
              validator: (text) {
                if (text == null || text.isEmpty) {
                  return '??????????????? ??????????????????.';
                }
                if (!RegExp(
                        r"^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$")
                    .hasMatch(text)) {
                  return '????????? ??????, ??????????????? ????????? 8 ~ 16?????? ??????????????????.';
                }
                return null;
              },
              decoration: InputDecoration(
                errorStyle: TextStyle(fontFamily: "Sub"),
                isCollapsed: true,
                hintText: "????????????",
                hintStyle: TextStyle(color: sColor, fontFamily: "Sub"),
                contentPadding: EdgeInsets.fromLTRB(20.w, 17.h, 10.w, 17.h),
                enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: sColor)),
                filled: true,
                fillColor: Colors.white,
                focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: btnColor)),
              ),
            ),
          ),
          TextFormField(
            // ?????????
            style: TextStyle(fontFamily: "Sub"),
            keyboardType: TextInputType.text,
            textInputAction: TextInputAction.next,
            cursorColor: btnColor,
            onChanged: (val) {
              nickname = val;
              setState(() => checkDupNickname = false);
            },
            autovalidateMode: _submitted
                ? AutovalidateMode.onUserInteraction
                : AutovalidateMode.disabled,
            validator: (text) {
              if (text == null || text.isEmpty || text.length > 10) {
                return '???????????? 10??? ????????? ??????????????????.';
              }
              if (!checkDupNickname) {
                return '????????? ??????????????? ????????????.';
              }
              return null;
            },
            decoration: InputDecoration(
              errorStyle: TextStyle(fontFamily: "Sub"),
              isCollapsed: true,
              hintText: "?????????",
              hintStyle: TextStyle(color: sColor, fontFamily: "Sub"),
              contentPadding: EdgeInsets.fromLTRB(20.w, 10.h, 10.w, 10.h),
              enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.all(Radius.circular(10)),
                  borderSide: BorderSide(color: sColor)),
              filled: true,
              fillColor: Colors.white,
              focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.all(Radius.circular(10)),
                  borderSide: BorderSide(color: btnColor)),
              suffix: ElevatedButton(
                onPressed: () async {
                  // ????????? ????????????
                  print(nickname);
                  setState(() => _submitted = true);
                  if (nickname == null ||
                      nickname!.isEmpty ||
                      nickname!.length > 10) {
                    return;
                  }
                  generalResponse result =
                      await apiSignup.checkNickname(nickname);
                  print(result.statusCode);
                  print(result.message);
                  if (result?.statusCode == 200) {
                    setState(() => checkDupNickname = true);
                    showDialog(
                      context: context,
                      builder: (BuildContext context) {
                        return CustomDialog("?????? ????????? ??????????????????.", null);
                      },
                    );
                  } else {
                    checkDupNickname = false;
                    showDialog(
                        context: context,
                        builder: (BuildContext context) {
                          return CustomDialog(result.message!, null);
                        });
                  }
                },
                style: ElevatedButton.styleFrom(
                  padding: EdgeInsets.all(0),
                  backgroundColor: btnColor,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                ),
                child: Text(
                  "????????????".toUpperCase(),
                  style: TextStyle(
                    fontFamily: "Sub",
                  ),
                ),
              ),
            ),
          ),
          Padding(
              // ??????
              padding: const EdgeInsets.symmetric(vertical: defaultPadding / 2),
              child: Column(children: <Widget>[
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    Expanded(
                        child: ListTile(
                      title: const Text(
                        '??????',
                        style: TextStyle(color: btnColor, fontFamily: "Sub"),
                      ),
                      leading: Radio<String>(
                        value: "M",
                        groupValue: gender,
                        fillColor: MaterialStateColor.resolveWith(
                            (states) => btnColor),
                        onChanged: (String? value) {
                          setState(() {
                            gender = value;
                          });
                        },
                      ),
                    )),
                    Expanded(
                        child: ListTile(
                      title: const Text(
                        '??????',
                        style: TextStyle(color: btnColor, fontFamily: "Sub"),
                      ),
                      leading: Radio<String>(
                        value: "F",
                        groupValue: gender,
                        fillColor: MaterialStateColor.resolveWith(
                            (states) => btnColor),
                        onChanged: (String? value) {
                          setState(() {
                            gender = value;
                          });
                        },
                      ),
                    )),
                  ],
                )
              ])),
          TextFormField(
            // ????????????
            style: TextStyle(fontFamily: "Sub"),
            keyboardType: TextInputType.number,
            textInputAction: TextInputAction.next,
            cursorColor: btnColor,
            onChanged: (val) {
              phone = val;
            },
            autovalidateMode: _submitted
                ? AutovalidateMode.onUserInteraction
                : AutovalidateMode.disabled,
            validator: (text) {
              if (text == null ||
                  text.length < 10 ||
                  text.length > 11 ||
                  !RegExp(r'^010?([0-9]{4})?([0-9]{4})$').hasMatch(text)) {
                return '??????????????? ??????????????????.';
              }
              return null;
            },
            decoration: InputDecoration(
              errorStyle: TextStyle(fontFamily: "Sub"),
              isCollapsed: true,
              hintText: "????????????",
              hintStyle: TextStyle(color: sColor, fontFamily: "Sub"),
              contentPadding: EdgeInsets.fromLTRB(20.w, 17.h, 10.w, 17.h),
              enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.all(Radius.circular(10)),
                  borderSide: BorderSide(color: sColor)),
              filled: true,
              fillColor: Colors.white,
              focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.all(Radius.circular(10)),
                  borderSide: BorderSide(color: btnColor)),
            ),
          ),
          Padding(
            // ?????????
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: TextFormField(
              style: TextStyle(fontFamily: "Sub"),
              textInputAction: TextInputAction.done,
              keyboardType: TextInputType.emailAddress,
              obscureText: false,
              cursorColor: btnColor,
              onChanged: (val) {
                email = val;
              },
              autovalidateMode: _submitted
                  ? AutovalidateMode.onUserInteraction
                  : AutovalidateMode.disabled,
              validator: (text) {
                if (text == null ||
                    text.isEmpty ||
                    text.length > 50 ||
                    !RegExp(r"^[a-zA-Z0-9.a-zA-Z0-9.!#$%&'*+-/=?^_`{|}~]+@[a-zA-Z0-9]+\.[a-zA-Z]+")
                        .hasMatch(text)) {
                  return '???????????? ??????????????????.';
                }
                return null;
              },
              decoration: InputDecoration(
                errorStyle: TextStyle(fontFamily: "Sub"),
                isCollapsed: true,
                hintText: "?????????",
                hintStyle: TextStyle(color: sColor, fontFamily: "Sub"),
                contentPadding: EdgeInsets.fromLTRB(20.w, 17.h, 10.w, 17.h),
                enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: sColor)),
                filled: true,
                fillColor: Colors.white,
                focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: btnColor)),
              ),
            ),
          ),
          Padding(
            // ?????? ??????
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: Column(
              children: <Widget>[
                Row(
                  children: [
                    Checkbox(
                      value: agreed,
                      onChanged: (value) {
                        setState(() {
                          agreed = value!;
                        });
                      },
                    ),
                    RichText(
                      text: TextSpan(
                        children: [
                          TextSpan(
                            style: TextStyle(
                                color: sColor,
                                decoration: TextDecoration.underline,
                                fontFamily: "Sub"),
                            text: "??????",
                            recognizer: TapGestureRecognizer()
                              ..onTap = () async {
                                final url = Uri.parse(
                                    'https://1vl.notion.site/e240cebfbf0049d9a7098953608bd1c7');
                                if (await canLaunchUrl(url)) {
                                  await launchUrl(url);
                                }
                              },
                          ),
                          agreed
                              ? TextSpan(
                                  style: TextStyle(
                                      color: sColor.withOpacity(0.8),
                                      fontFamily: "Sub"),
                                  text: "??? ?????????????????????.",
                                )
                              : TextSpan(
                                  style: TextStyle(
                                      color: Colors.deepOrange,
                                      fontFamily: "Sub"),
                                  text: "??? ???????????? ????????? ?????????.",
                                ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          Container(
              margin: EdgeInsets.fromLTRB(0, 0, 0, 10),
              child: Divider(color: sColor, thickness: 2.0)),
          Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  Container(
                    // color: const Color(0xffd0cece),
                    width: MediaQuery.of(context).size.width / 5,
                    height: MediaQuery.of(context).size.width / 5,
                    child: Center(
                        child: profileImage == null
                            ? Text('')
                            : new CircleAvatar(
                                backgroundImage:
                                    new FileImage(File(profileImage!.path)),
                                radius: 200.0,
                              )),
                    decoration:
                        BoxDecoration(color: sColor, shape: BoxShape.circle),
                  ),
                  Container(
                    child: ElevatedButton.icon(
                      onPressed: () {
                        chooseImage(); // call choose image function
                      },
                      icon: Icon(Icons.image),
                      style: ElevatedButton.styleFrom(
                          backgroundColor: btnColor,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(5.0),
                          )),
                      label: Text(
                        "????????? ????????? (??????)",
                        style: TextStyle(
                          fontFamily: "Sub",
                        ),
                      ),
                    ),
                  ),
                ],
              )
            ],
          ),
          Padding(
            // ????????????
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: TextFormField(
              style: TextStyle(fontFamily: "Sub"),
              keyboardType: TextInputType.multiline,
              maxLines: 4,
              textInputAction: TextInputAction.done,
              cursorColor: btnColor,
              onChanged: (val) {
                introduce = val;
              },
              autovalidateMode: _submitted
                  ? AutovalidateMode.onUserInteraction
                  : AutovalidateMode.disabled,
              validator: (text) {
                if (text == null) {
                  return null;
                }
                if (text.length > 1000) {
                  return '??????????????? ?????? 1000????????? ????????? ??? ?????????.';
                }
                return null;
              },
              decoration: InputDecoration(
                errorStyle: TextStyle(fontFamily: "Sub"),
                isCollapsed: true,
                hintText: "???????????? (??????)",
                hintStyle: TextStyle(color: sColor, fontFamily: "Sub"),
                contentPadding: EdgeInsets.fromLTRB(20.w, 20.h, 20.w, 20.h),
                enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: sColor)),
                filled: true,
                fillColor: Colors.white,
                focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(10)),
                    borderSide: BorderSide(color: btnColor)),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: Container(
              height: 48,
              width: double.maxFinite,
              child: Hero(
                tag: "signUp_btn",
                child: ElevatedButton(
                  onPressed: () {
                    if (agreed) {
                      _submit();
                    } else {
                      showDialog(
                          context: context,
                          builder: (BuildContext context) {
                            return CustomDialog("???????????? ????????? ????????? ??????????????????.", null);
                          });
                    }
                  },
                  style: ElevatedButton.styleFrom(
                      backgroundColor: btnColor,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(25.0),
                      )),
                  child: Text(
                    "????????????".toUpperCase(),
                    style: TextStyle(
                      fontFamily: "Sub",
                    ),
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(height: defaultPadding / 2),
        ],
      ),
    );
  }
}
