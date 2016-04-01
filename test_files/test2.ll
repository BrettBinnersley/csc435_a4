
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"

target triple = "x86_64-unknown-linux-gnu"

@.str.1 = private unnamed_addr constant [12 x i8] c"hello world\00", align 1
@.str.2 = private unnamed_addr constant [5 x i8] c"%s \0a\00", align 1
@.str.3 = private unnamed_addr constant [15 x i8] c"factorial(1) =\00", align 1
@.str.4 = private unnamed_addr constant [8 x i8] c"%s %d \0a\00", align 1
@.str.5 = private unnamed_addr constant [15 x i8] c"factorial(6) =\00", align 1
declare i64 @printf(i8*, ...) #1
declare void @llvm.memset.p0i8.i64(i8*, i8, i64, i32, i1)

; Function Attrs: nounwind uwtable
define void @main() {
entry:
  %0 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @.str.2, i32 0, i32 0) , [12 x i8]* @.str.1)
  %1 = alloca i32, align 8 ; x
  %2 =  call i32 @factorial(i32 1)
  store i32 %2, i32* %1, align 4
  %3 = load i32, i32* %1
  %4 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([8 x i8], [8 x i8]* @.str.4, i32 0, i32 0) , [15 x i8]* @.str.3, i32 %3)
  %5 =  call i32 @factorial(i32 6)
  store i32 %5, i32* %1, align 4
  %6 = load i32, i32* %1
  %7 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([8 x i8], [8 x i8]* @.str.4, i32 0, i32 0) , [15 x i8]* @.str.5, i32 %6)
  ret void
}
; Function Attrs: nounwind uwtable
define i32 @factorial(i32 %k) {
entry:
  %k.addr = alloca i32, align 4
  store i32 %k, i32* %k.addr, align 4
  %0 = load i32, i32* %k.addr
  %1 = icmp slt i32 %0, 2
  br i1 %1, label %then.0, label %else.1
then.0:
  ret i32 1
dead.3:
  br label %endif.2
else.1:
  br label %endif.2
endif.2:
  %2 = load i32, i32* %k.addr
  %3 = sub i32 %2, 1
  %4 =  call i32 @factorial(i32 %3)
  %5 = load i32, i32* %k.addr
  %6 = mul i32 %5, %4
  ret i32 %6
dead.4:
  ret i32 0
}


attributes #0 = { nounwind uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

