//
//  ATAccessoryView.swift
//  Dating
//
//  Created by Minh Tường on 17/06/2022.
//

import MTSDK
import UIKit

open class ATAccessoryView: UIInputView {
    public override init(frame: CGRect, inputViewStyle: UIInputView.Style) {
        textView = UITextView(frame: frame)
        super.init(frame: frame, inputViewStyle: .keyboard)
        self.setupView()
    }
    
    public required init?(coder: NSCoder) { fatalError()}
    
    open override var intrinsicContentSize: CGSize {
        let textSize = self.textView.sizeThatFits(CGSize(width: self.textView.bounds.width, height: CGFloat.greatestFiniteMagnitude))
        let height = min (self.tvMaxHeight, textSize.height + 16)
        return CGSize(width: self.bounds.width, height: height)
    }

    
    //Variables
    public var maxLine: Int = 5 {
        didSet {
            let textSize = self.textView.sizeThatFits(CGSize(width: self.textView.bounds.width, height: CGFloat.greatestFiniteMagnitude))
            self.tvMaxHeight = (textSize.height * self.maxLine.cgFloat)
        }
    }
    public var buttonTitle: String = "Send" {
        didSet {
            self.sendButton.setTitle(buttonTitle, for: .normal)
            if let font = self.sendButton.titleLabel?.font {
                let width = buttonTitle.width(height: 40, font: font)
                self.tvMaxWidth = width + 16 // padding
            }
        }
    }
    
    public var doneHandle: ((String?) -> Void)?
    public let textView: UITextView
    public let sendButton = UIButton()
    
    private var tvMaxWidth: CGFloat = 79
    private var tvMaxHeight: CGFloat = 79
    
}

public extension ATAccessoryView {
    
}

extension ATAccessoryView: UITextViewDelegate {
    public func textViewDidChange(_ textView: UITextView) {
        self.invalidateIntrinsicContentSize()
        UIView.animate(withDuration: 0.39, delay: 0, options: .curveEaseInOut, animations: {
            self.layoutIfNeeded()
        })
        
        if let text = textView.text, !text.trimmingCharacters(in: .whitespaces).isEmpty {
            self.sendButton.isHidden = false
            self.sendButton.snp.updateConstraints {
                $0.width.equalTo(self.tvMaxWidth)
            }
            UIView.animate(withDuration: 0.39, delay: 0, options: .curveEaseInOut, animations: {
                self.layoutIfNeeded()
            })
        } else {
            self.sendButton.snp.updateConstraints {
                $0.width.equalTo(0)
            }
            self.sendButton.isHidden = true
            UIView.animate(withDuration: 0.39, delay: 0, options: .curveEaseInOut, animations: {
                self.layoutIfNeeded()
            })
        }
    }
    
}


//MARK: Functions
public extension ATAccessoryView {
   private func setupView() {
       self.autoresizingMask = UIView.AutoresizingMask.flexibleHeight
       
       self.maxLine = 3
       
       sendButton >>> self >>> {
           $0.snp.makeConstraints {
              $0.trailing.bottom.equalToSuperview().offset(-8)
              $0.height.equalTo(40)
              $0.width.equalTo(0)
           }
           $0.isHidden = true
           $0.setTitle("Send", for: .normal)
           let font = UIFont.boldSystemFont(ofSize: 17)
           $0.titleLabel?.font = font
           let width = buttonTitle.width(height: 40, font: font)
           self.tvMaxWidth = width + 16
           $0.handle {
               if let handle = self.doneHandle {
                   handle(self.textView.text)
               }
           }
      }
       
        textView >>> self >>> {
            $0.snp.makeConstraints {
                $0.leading.top.equalToSuperview().offset(8)
                $0.bottom.equalToSuperview().offset(-8)
                $0.trailing.equalTo(sendButton.snp.leading).offset(-8)
            }
            $0.textContainer.lineFragmentPadding = 0
            $0.textContainerInset = UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 0)
            $0.backgroundColor = .clear
            $0.font = UIFont(name: FNames.medium, size: 17)
            $0.delegate = self
            $0.layer.borderWidth = 1
            $0.layer.borderColor = UIColor.lightGray.cgColor
            $0.layer.cornerRadius = 12
        }
        
       self.updateUI()
    }
    
    private func updateUI() {
        if self.traitCollection.userInterfaceStyle == .dark {
            self.textView.textColor = .white
            self.textView.layer.borderColor = UIColor.white.cgColor
            self.sendButton.setTitleColor(.white, for: .normal)
            self.sendButton.setTitleColor(.white.withAlphaComponent(0.5), for: .highlighted)
        } else {
            self.textView.textColor = .black
            self.textView.layer.borderColor = UIColor.lightGray.cgColor
            self.sendButton.setTitleColor(.black, for: .normal)
            self.sendButton.setTitleColor(.black.withAlphaComponent(0.5), for: .highlighted)
        }
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        self.updateUI()
    }
}

