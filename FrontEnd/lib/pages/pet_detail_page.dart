import 'package:GSSL/components/pet/pet_detail.dart';
import 'package:GSSL/constants.dart';
import 'package:GSSL/responsive.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

class PetDetailScreen extends StatelessWidget {
  const PetDetailScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('반려견 정보'),
        titleTextStyle: TextStyle(fontFamily: "Sub", fontSize: 25.sp),
        foregroundColor: nWColor,
        backgroundColor: btnColor,
        centerTitle: true,
      ),
      backgroundColor: nWColor,
      body: GestureDetector(
        onTap: () {
          FocusScope.of(context).unfocus();
        },
        child: SingleChildScrollView(
          child: Responsive(
            mobile: const MobileSignupScreen(),
            desktop: Row(
              children: [
                Expanded(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: const [
                      SizedBox(
                        width: 450,
                        child: PetDetail(),
                      ),
                      SizedBox(height: defaultPadding / 2),
                      // SocalSignUp()
                    ],
                  ),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class MobileSignupScreen extends StatelessWidget {
  const MobileSignupScreen({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        Row(
          children: const [
            Spacer(),
            Expanded(
              flex: 8,
              child: PetDetail(),
            ),
            Spacer(),
          ],
        ),
        // const SocalSignUp()
      ],
    );
  }
}
