import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

class health_magazine extends StatelessWidget {
  const health_magazine({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 500.sm,
      width: 480.sm,
      padding: EdgeInsets.all(5.sm),
      // color: Colors.red,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Column(
            children: [
              magazineBox(),
              magazineBox(),
            ],
          ),
          Column(
            children: [
              magazineBox(),
              magazineBox(),
            ],
          )
        ],
      ),
    );
  }
}

class magazineBoxTitle extends StatelessWidget {
  const magazineBoxTitle({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
        width: 0.45.sw,
        height: 230.sm,
        margin: EdgeInsets.fromLTRB(0, 5.h, 10.w, 0),
        child: Column(
          children: [
            Image.asset('assets/images/tooth/001.png'),
            Container(
              child: Row(
                children: [
                  Padding(
                    padding: EdgeInsets.fromLTRB(3.5.w, 0, 3.5.w, 0),
                    child: Text(
                        style: TextStyle(
                          color: Color(0xff424242),
                          fontFamily: "sub",
                          fontSize: 15.sp,
                        ),
                        "건강"),
                  ),
                  Padding(
                    padding: EdgeInsets.fromLTRB(0, 8.h, 0, 0),
                    child: Text(
                        style: TextStyle(
                          fontFamily: "sub",
                          fontSize: 15.sp,
                        ),
                        "반짝 반짝 건치를 위한\n양치 꿀팁 대방출!"),
                  ),
                ],
              ),
            )
          ],
        ));
  }
}

class magazineBox extends StatelessWidget {
  const magazineBox({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        showModalBottomSheet(
          context: context,
          isScrollControlled: true,
          builder: (BuildContext context) {
            return Container(
                height: 0.8.sh,
                decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.only(
                      topLeft: Radius.circular(10),
                      topRight: Radius.circular(10),
                    )),
                child: magazineBoxContent()); // 내부
          },
        );
      },
      child: magazineBoxTitle(), // 보이는 화면
    );
  }
}

class magazineBoxContent extends StatelessWidget {
  const magazineBoxContent({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return ListView(
      children: [
        photos(
          adrs: "assets/images/tooth/001.png",
        ),
        photos(
          adrs: "assets/images/tooth/002.png",
        ),
        photos(
          adrs: "assets/images/tooth/003.png",
        ),
        photos(
          adrs: "assets/images/tooth/004.png",
        ),
        photos(
          adrs: "assets/images/tooth/005.png",
        ),
        photos(
          adrs: "assets/images/tooth/006.png",
        ),
      ],
    );
  }
}

class photos extends StatelessWidget {
  const photos({Key? key, this.adrs}) : super(key: key);
  final adrs;

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Image.asset(adrs),
    );
  }
}
