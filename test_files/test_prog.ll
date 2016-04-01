
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"

target triple = "x86_64-unknown-linux-gnu"

@glob.1 = common global i32 97, align 4
@k.2 = constant i32 33, align 4
@.str.3 = private unnamed_addr constant [24 x i8] c"My favorite numbers are\00", align 1
@.str.4 = private unnamed_addr constant [14 x i8] c"%s %d %d %g \0a\00", align 1
@.str.5 = private unnamed_addr constant [10 x i8] c"Surprise!\00", align 1
@.str.6 = private unnamed_addr constant [5 x i8] c"%s \0a\00", align 1
declare i64 @printf(i8*, ...) #1
declare void @llvm.memset.p0i8.i64(i8*, i8, i64, i32, i1)

; Function Attrs: nounwind uwtable
define void @main() {
entry:
  %0 = alloca i32, align 8 ; x
  %1 = load i32, i32* @k.2
  store i32 %1, i32* %0, align 4
  %2 = load i32, i32* %0
  %3 = add i32 %2, 1
  store i32 %3, i32* %0, align 4
  %4 = load i32, i32* @glob.1
  %5 = mul i32 %4, 2
  store i32 %5, i32* @glob.1, align 4
  %6 = load i32, i32* %0
  %7 = icmp sgt i32 %6, 0
  br i1 %7, label %then.0, label %else.1
then.0:
  %8 = alloca double, align 8 ; pi
  store double 3.14159, double* %8
  %9 = load i32, i32* %0
  %10 = load i32, i32* @glob.1
  %11 = load double, double* %8
  %12 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([14 x i8], [14 x i8]* @.str.4, i32 0, i32 0) , [24 x i8]* @.str.3, i32 %9, i32 %10, double %11)
  br label %endif.2
else.1:
  %13 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @.str.6, i32 0, i32 0) , [10 x i8]* @.str.5)
  br label %endif.2
endif.2:
  ret void
}


attributes #0 = { nounwind uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

